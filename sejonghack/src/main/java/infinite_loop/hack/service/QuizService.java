package infinite_loop.hack.service;

import infinite_loop.hack.dto.QuizDtos.AttemptsTodayRes;
import infinite_loop.hack.dto.QuizDtos.CreateSessionRes;
import infinite_loop.hack.dto.QuizDtos.AnswerOneRes;
import infinite_loop.hack.domain.QuizSession;
import infinite_loop.hack.domain.QuizSession.Status;
import infinite_loop.hack.domain.QuizSessionItem;
import infinite_loop.hack.exception.ActiveSessionConflictException;
import infinite_loop.hack.exception.SessionClosedException;
import infinite_loop.hack.openai.OpenAiClient;
import infinite_loop.hack.openai.OpenAiClient.GptQuizResponse;
import infinite_loop.hack.repository.QuizSessionItemRepository;
import infinite_loop.hack.repository.QuizSessionRepository;
import infinite_loop.hack.support.QuizConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Quiz service (one-by-one flow)
 * - No time-based expiry (API expiresAt is null)
 * - No daily attempt limits (attemptsLeftToday = -1)
 * - Session closes when all questions are answered -> Status.SUBMITTED
 * - Starting with existing ACTIVE session -> 409 via ActiveSessionConflictException
 */
@Service
@RequiredArgsConstructor
public class QuizService {

    // ===== Settings =====
    private static final int QUESTIONS_PER_SESSION = 3;
    private static final int POINT_PER_CORRECT = 10;

    // If DB column quiz_session.expires_at is NOT NULL, store a far-future value.
    // API responses will still expose expiresAt=null so the UI treats it as "no expiry".
    private static final boolean USE_FAR_FUTURE_FOR_DB = true;
    private static final Instant FAR_FUTURE = Instant.parse("2100-01-01T00:00:00Z");

    // ===== Deps =====
    private final OpenAiClient openai;
    private final QuizSessionRepository sessionRepo;
    private final QuizSessionItemRepository itemRepo;
    private final PointService pointService;

    // ===== Helpers =====
    private Instant persistableExpiresAt() {
        return USE_FAR_FUTURE_FOR_DB ? FAR_FUTURE : null;
    }
    private Instant publicExpiresAt(Instant raw) {
        // TTL disabled -> always expose null to clients
        return null;
    }

    /**
     * Start a new session.
     * - If ACTIVE session exists -> throw ActiveSessionConflictException (409 with continuation)
     * - No TTL, no daily limit
     */
    @Transactional
    public CreateSessionRes startSession(Long userId) {
        Optional<QuizSession> existing = sessionRepo.findFirstByUserIdAndStatus(userId, Status.ACTIVE);
        if (existing.isPresent()) {
            throw new ActiveSessionConflictException(existing.get().getId());
        }

        // Prepare questions (3 items)
        String category = QuizConstants.pickRandomCategory();
        GptQuizResponse g = openai.createThreeQuestions(category);
        if (g == null || g.questions == null || g.questions.size() != QUESTIONS_PER_SESSION) {
            throw new IllegalStateException("QUIZ_GENERATION_FAILED");
        }

        // Create session (no real expiry)
        QuizSession s = new QuizSession();
        s.setUserId(userId);
        s.setCategory(category);
        s.setNumQuestions(QUESTIONS_PER_SESSION);
        s.setStartedAt(Instant.now());
        s.setExpiresAt(persistableExpiresAt()); // DB safe; API will show null
        s.setStatus(Status.ACTIVE);
        s.setTotalAwardedPoints(0);
        s = sessionRepo.save(s);

        // Persist items
        List<CreateSessionRes.Item> items = new ArrayList<>();
        int order = 1;
        for (var q : g.questions) {
            // Defensive check for 4 choices
            if (q.choices == null || q.choices.size() != 4) {
                throw new IllegalStateException("QUIZ_GENERATION_FAILED");
            }

            QuizSessionItem it = new QuizSessionItem();
            it.setSessionId(s.getId());
            it.setItemOrder(order);
            it.setPrompt(q.prompt);
            it.setChoice1(q.choices.get(0));
            it.setChoice2(q.choices.get(1));
            it.setChoice3(q.choices.get(2));
            it.setChoice4(q.choices.get(3));
            it.setCorrectIndex(q.correct_index); // 1..4
            itemRepo.save(it);

            items.add(new CreateSessionRes.Item(
                    it.getId(),
                    order,
                    it.getPrompt(),
                    List.of(it.getChoice1(), it.getChoice2(), it.getChoice3(), it.getChoice4())
            ));
            order++;
        }

        // attempts: unlimited (-1)
        return new CreateSessionRes(
                s.getId(),
                publicExpiresAt(s.getExpiresAt()),
                s.getNumQuestions(),
                s.getCategory(),
                -1, // unlimited
                items
        );
    }

    /** ACTIVE 세션 스냅샷 (없으면 Optional.empty) */
    @Transactional(readOnly = true)
    public Optional<CreateSessionRes> getActiveSessionSnapshot(Long userId) {
        return sessionRepo.findFirstByUserIdAndStatus(userId, Status.ACTIVE)
                .map(this::toSnapshot);
    }

    /** 특정 세션 스냅샷 (본인 소유 아닐 경우 empty) */
    @Transactional(readOnly = true)
    public Optional<CreateSessionRes> getSessionSnapshot(Long userId, Long sessionId) {
        return sessionRepo.findById(sessionId)
                .filter(s -> s.getUserId().equals(userId))
                .map(this::toSnapshot);
    }

    private CreateSessionRes toSnapshot(QuizSession s) {
        var itemsDb = itemRepo.findBySessionIdOrderByItemOrder(s.getId());
        List<CreateSessionRes.Item> items = new ArrayList<>();
        for (QuizSessionItem it : itemsDb) {
            items.add(new CreateSessionRes.Item(
                    it.getId(),
                    it.getItemOrder(),
                    it.getPrompt(),
                    List.of(it.getChoice1(), it.getChoice2(), it.getChoice3(), it.getChoice4())
            ));
        }
        return new CreateSessionRes(
                s.getId(),
                publicExpiresAt(s.getExpiresAt()), // expose null
                s.getNumQuestions(),
                s.getCategory(),
                -1, // unlimited
                items
        );
    }

    /**
     * Submit one answer (one-by-one).
     * - Validates ownership, status, and item-session relation
     * - Marks correct/incorrect and accumulates points
     * - When all answered, closes session (SUBMITTED) and grants points once
     * - On re-submission to a closed session, throws SessionClosedException -> 409
     */
    @Transactional
    public AnswerOneRes answerOne(Long userId, Long sessionId, Long itemId, Integer answerIdx) {
        if (answerIdx == null || answerIdx < 1 || answerIdx > 4) {
            throw new IllegalArgumentException("INVALID_ANSWER_INDEX");
        }

        QuizSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("SESSION_NOT_FOUND"));
        if (!s.getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("FORBIDDEN");
        }

        // Closed session? (SUBMITTED)
        if (s.getStatus() != Status.ACTIVE) {
            throw new SessionClosedException(s.getId(), s.getStatus().name());
        }

        QuizSessionItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("ITEM_NOT_FOUND"));
        if (!item.getSessionId().equals(sessionId)) {
            throw new IllegalArgumentException("ITEM_NOT_IN_SESSION");
        }

        boolean wasAnswered = (item.getUserAnswerIndex() != null);
        boolean correct;
        int award = 0;

        if (!wasAnswered) {
            item.setUserAnswerIndex(answerIdx);
            correct = (answerIdx == item.getCorrectIndex());
            item.setIsCorrect(correct);
            if (correct) {
                award = POINT_PER_CORRECT;
                item.setAwardedPoints(award);
                s.setTotalAwardedPoints(s.getTotalAwardedPoints() + award);
            } else {
                item.setAwardedPoints(0); // prevent future NPEs
            }
            itemRepo.save(item);
            sessionRepo.save(s);
        } else {
            correct = Boolean.TRUE.equals(item.getIsCorrect());
            Integer ap = item.getAwardedPoints();
            award = (ap != null ? ap : 0); // null-safe when previously wrong
        }

        List<QuizSessionItem> items = itemRepo.findBySessionIdOrderByItemOrder(sessionId);
        long answered = items.stream().filter(it -> it.getUserAnswerIndex() != null).count();
        int total = items.size();

        boolean completed = (answered >= total);
        Integer nextOrder = null;

        if (!completed) {
            nextOrder = items.stream()
                    .filter(it -> it.getUserAnswerIndex() == null)
                    .min(Comparator.comparingInt(QuizSessionItem::getItemOrder))
                    .map(QuizSessionItem::getItemOrder)
                    .orElse(null);
        } else {
            // Close session
            s.setStatus(Status.SUBMITTED);
            // If you have finishedAt column: s.setFinishedAt(Instant.now());
            sessionRepo.save(s);

            // Grant points once at completion
            if (s.getTotalAwardedPoints() > 0) {
                pointService.addPoints(userId, s.getTotalAwardedPoints(), "QUIZ");
            }
        }

        return new AnswerOneRes(
                s.getId(),
                item.getId(),
                correct,
                award,
                (int) answered,
                total,
                completed,
                nextOrder,
                Instant.now()
        );
    }

    /**
     * Attempts today (kept for backward compatibility).
     * Since limits are disabled, always returns unlimited.
     */
    @Transactional(readOnly = true)
    public AttemptsTodayRes getAttemptsToday(Long userId) {
        return new AttemptsTodayRes(
                -1,          // attemptsLeftToday = -1
                true,        // unlimited
                null         // window info not applicable
        );
    }

    /**
     * Continuation info for 409 response headers/body when an ACTIVE session already exists.
     */
    @Transactional(readOnly = true)
    public ContinuationInfo getContinuationInfo(Long sessionId) {
        QuizSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("SESSION_NOT_FOUND"));

        List<QuizSessionItem> items = itemRepo.findBySessionIdOrderByItemOrder(sessionId);
        int total = items.size();
        int answered = (int) items.stream().filter(it -> it.getUserAnswerIndex() != null).count();
        Integer nextOrder = items.stream()
                .filter(it -> it.getUserAnswerIndex() == null)
                .min(Comparator.comparingInt(QuizSessionItem::getItemOrder))
                .map(QuizSessionItem::getItemOrder)
                .orElse(null);

        return new ContinuationInfo(
                s.getId(),
                s.getStatus().name(),
                publicExpiresAt(s.getExpiresAt()), // expose null
                nextOrder,
                answered,
                total
        );
    }

    /** Small immutable view used by exception handler for 409 continuation details. */
    public record ContinuationInfo(
            Long sessionId,
            String status,
            Instant expiresAt,
            Integer nextItemOrder,
            int answeredCount,
            int total
    ) {}
}
