package infinite_loop.hack.service;

import infinite_loop.hack.dto.QuizDtos.*;
import infinite_loop.hack.domain.QuizSession;
import infinite_loop.hack.domain.QuizSession.Status;
import infinite_loop.hack.domain.QuizSessionItem;
import infinite_loop.hack.exception.ActiveSessionConflictException;
import infinite_loop.hack.openai.OpenAiClient;
import infinite_loop.hack.openai.OpenAiClient.GptQuizResponse;
import infinite_loop.hack.repository.QuizSessionItemRepository;
import infinite_loop.hack.repository.QuizSessionRepository;
import infinite_loop.hack.support.QuizConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Duration SESSION_TTL = Duration.ofMinutes(10);
    private static final int QUESTIONS_PER_SESSION = 3;
    private static final int POINT_PER_CORRECT = 10;

    private final OpenAiClient openai;
    private final QuizSessionRepository sessionRepo;
    private final QuizSessionItemRepository itemRepo;
    private final PointService pointService;

    /**
     * Start a new session (throws ActiveSessionConflictException if active session exists).
     */
    @Transactional
    public CreateSessionRes startSession(Long userId) {
        // If an ACTIVE and not-expired session exists, return 409 via exception handler.
        Optional<QuizSession> existing = sessionRepo.findFirstByUserIdAndStatus(userId, Status.ACTIVE);
        if (existing.isPresent() && !existing.get().isExpired()) {
            throw new ActiveSessionConflictException(existing.get().getId());
        }
        // If an ACTIVE session exists but expired, mark it EXPIRED.
        existing.ifPresent(s -> {
            if (s.isExpired()) {
                s.setStatus(Status.EXPIRED);
                sessionRepo.save(s);
            }
        });

        // 1-day limit: 3 sessions/day (KST)
        LocalDate today = QuizConstants.todayKST();
        Instant start = today.atStartOfDay(KST).toInstant();
        Instant end   = today.plusDays(1).atStartOfDay(KST).toInstant();
        int usedToday = sessionRepo.findByUserIdAndStartedAtBetween(userId, start, end).size();
        if (usedToday >= 3) {
            throw new IllegalStateException("DAILY_LIMIT_EXCEEDED");
        }

        String category = QuizConstants.pickRandomCategory();
        GptQuizResponse g = openai.createThreeQuestions(category);
        if (g == null || g.questions == null || g.questions.size() != QUESTIONS_PER_SESSION) {
            throw new IllegalStateException("QUIZ_GENERATION_FAILED");
        }

        QuizSession s = new QuizSession();
        s.setUserId(userId);
        s.setCategory(category);
        s.setNumQuestions(QUESTIONS_PER_SESSION);
        s.setStartedAt(Instant.now());
        s.setExpiresAt(Instant.now().plus(SESSION_TTL));
        s.setStatus(Status.ACTIVE);
        s.setTotalAwardedPoints(0);
        s = sessionRepo.save(s);

        List<CreateSessionRes.Item> items = new ArrayList<>();
        int order = 1;
        for (var q : g.questions) {
            QuizSessionItem item = new QuizSessionItem();
            item.setSessionId(s.getId());
            item.setItemOrder(order);
            item.setPrompt(q.prompt);
            item.setChoice1(q.choices.get(0));
            item.setChoice2(q.choices.get(1));
            item.setChoice3(q.choices.get(2));
            item.setChoice4(q.choices.get(3));
            item.setCorrectIndex(q.correct_index); // 1..4
            itemRepo.save(item);

            items.add(new CreateSessionRes.Item(
                    item.getId(),
                    order,
                    item.getPrompt(),
                    List.of(item.getChoice1(), item.getChoice2(), item.getChoice3(), item.getChoice4())
            ));
            order++;
        }

        int attemptsLeft = Math.max(0, 3 - usedToday - 1);
        return new CreateSessionRes(
                s.getId(), s.getExpiresAt(), s.getNumQuestions(), s.getCategory(),
                attemptsLeft, items
        );
    }

    /**
     * Return active session snapshot for the user.
     */
    @Transactional(readOnly = true)
    public Optional<CreateSessionRes> getActiveSessionSnapshot(Long userId) {
        return sessionRepo.findFirstByUserIdAndStatus(userId, Status.ACTIVE)
                .filter(s -> !s.isExpired())
                .map(this::toSnapshot);
    }

    /**
     * Return snapshot for specific session if it belongs to user.
     */
    @Transactional(readOnly = true)
    public Optional<CreateSessionRes> getSessionSnapshot(Long userId, Long sessionId) {
        return sessionRepo.findById(sessionId)
                .filter(s -> s.getUserId().equals(userId))
                .map(this::toSnapshot);
    }

    private CreateSessionRes toSnapshot(QuizSession s) {
        List<QuizSessionItem> items = itemRepo.findBySessionIdOrderByItemOrder(s.getId());
        List<CreateSessionRes.Item> out = new ArrayList<>();
        for (QuizSessionItem it : items) {
            out.add(new CreateSessionRes.Item(
                    it.getId(),
                    it.getItemOrder(),
                    it.getPrompt(),
                    List.of(it.getChoice1(), it.getChoice2(), it.getChoice3(), it.getChoice4())
            ));
        }
        // Attempts left (based on sessions started today)
        LocalDate today = QuizConstants.todayKST();
        Instant start = today.atStartOfDay(KST).toInstant();
        Instant end   = today.plusDays(1).atStartOfDay(KST).toInstant();
        int usedToday = sessionRepo.findByUserIdAndStartedAtBetween(s.getUserId(), start, end).size();
        int attemptsLeft = Math.max(0, 3 - usedToday);

        return new CreateSessionRes(
                s.getId(), s.getExpiresAt(), s.getNumQuestions(), s.getCategory(),
                attemptsLeft, out
        );
    }

    /**
     * Submit one answer (one-by-one flow).
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
        if (s.isExpired()) {
            s.setStatus(Status.EXPIRED);
            sessionRepo.save(s);
            throw new IllegalStateException("SESSION_EXPIRED");
        }
        if (s.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("SESSION_NOT_ACTIVE");
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
            }
            itemRepo.save(item);
            sessionRepo.save(s);
        } else {
            correct = item.getIsCorrect() != null && item.getIsCorrect();
            award = item.getAwardedPoints();
        }

        // Progress calculation
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
            // finalize session
            s.setStatus(Status.SUBMITTED);
            sessionRepo.save(s);
            // award accumulated points to user (once)
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
     * Utility used by exception handler to compute continuation info for an ACTIVE session.
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
                s.getExpiresAt(),
                nextOrder,
                answered,
                total
        );
    }

    /** Small DTO used internally by ApiExceptionHandler. */
    public record ContinuationInfo(
            Long sessionId,
            String status,
            Instant expiresAt,
            Integer nextItemOrder,
            int answeredCount,
            int total
    ) {}
}
