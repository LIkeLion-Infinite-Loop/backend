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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {
    private final QuizSessionRepository sessionRepo;
    private final QuizSessionItemRepository itemRepo;
    private final OpenAiClient openai;

    private static final int QUESTIONS_PER_SESSION = 3;
    private static final int POINTS_PER_CORRECT = 10;

    /** Attempts are unlimited now; retain contract for callers. */
    @Transactional(readOnly = true)
    public AttemptsTodayRes getAttemptsToday(Long userId) {
        return new AttemptsTodayRes(-1, true, null);
    }

    /** Create a new session; if an ACTIVE session exists, throw 409 (handled globally). */
    public CreateSessionRes startSession(Long userId) {
        Optional<QuizSession> active = sessionRepo.findFirstByUserIdAndStatus(userId, Status.ACTIVE);
        if (active.isPresent()) {
            throw new ActiveSessionConflictException(active.get().getId());
        }

        String category = QuizConstants.pickRandomCategory();
        // GPT returns exactly 3 questions for the given category.
        GptQuizResponse g = openai.createThreeQuestions(category);

        QuizSession s = new QuizSession();
        s.setUserId(userId);
        s.setCategory(category);
        s.setNumQuestions(QUESTIONS_PER_SESSION);
        s.setStartedAt(Instant.now());
        s.setExpiresAt(null); // no expiry persisted
        s.setStatus(Status.ACTIVE);
        s.setTotalAwardedPoints(0);
        s = sessionRepo.save(s);

        // Persist items
        List<CreateSessionRes.Item> items = new ArrayList<>();
        int order = 1;
        for (var q : g.questions) {
            if (q.choices == null || q.choices.size() != 4) {
                throw new IllegalStateException("GPT returned invalid choices length");
            }
            // Shuffle choices while tracking correct index
            List<String> choices = new ArrayList<>(q.choices);
            int originalCorrect = q.correct_index != null ? q.correct_index : 1;
            if (originalCorrect < 1 || originalCorrect > 4) originalCorrect = 1;

            // pair choices with original index
            List<int[]> idxMap = new ArrayList<>();
            for (int i = 0; i < choices.size(); i++) idxMap.add(new int[]{i + 1, i}); // [1-based original, 0-based position]

            Collections.shuffle(idxMap);
            String c1 = choices.get(idxMap.get(0)[1]);
            String c2 = choices.get(idxMap.get(1)[1]);
            String c3 = choices.get(idxMap.get(2)[1]);
            String c4 = choices.get(idxMap.get(3)[1]);
            int correctIndexAfterShuffle = -1;
            for (int i = 0; i < idxMap.size(); i++) {
                if (idxMap.get(i)[0] == originalCorrect) {
                    correctIndexAfterShuffle = i + 1;
                    break;
                }
            }
            if (correctIndexAfterShuffle <= 0) correctIndexAfterShuffle = 1;

            QuizSessionItem item = new QuizSessionItem();
            item.setSessionId(s.getId());
            item.setItemOrder(order++);
            item.setPrompt(q.prompt);
            item.setChoice1(c1);
            item.setChoice2(c2);
            item.setChoice3(c3);
            item.setChoice4(c4);
            item.setCorrectIndex(correctIndexAfterShuffle);
            item.setUserAnswerIndex(null);
            item.setIsCorrect(null);
            item.setAwardedPoints(0);
            item.setExplanation(q.explanation); // NEW: store explanation
            itemRepo.save(item);

            items.add(new CreateSessionRes.Item(
                    item.getId(),
                    item.getItemOrder(),
                    item.getPrompt(),
                    List.of(item.getChoice1(), item.getChoice2(), item.getChoice3(), item.getChoice4()),
                    null
            ));
        }

        return new CreateSessionRes(
                s.getId(),
                s.getStatus().name(),
                s.getCategory(),
                s.getStartedAt(),
                s.getExpiresAt(),
                s.getTotalAwardedPoints(),
                QUESTIONS_PER_SESSION,
                1, // next item is 1 at start
                items
        );
    }

    /** Snapshot of current ACTIVE session, if any. */
    @Transactional(readOnly = true)
    public Optional<CreateSessionRes> getActiveSessionSnapshot(Long userId) {
        return sessionRepo.findFirstByUserIdAndStatus(userId, Status.ACTIVE)
                .map(s -> toSnapshot(s, itemsByOrder(s.getId())));
    }

    /** Snapshot of a specific session (only if owned by user). */
    @Transactional(readOnly = true)
    public Optional<CreateSessionRes> getSessionSnapshot(Long userId, Long sessionId) {
        return sessionRepo.findById(sessionId)
                .filter(s -> Objects.equals(s.getUserId(), userId))
                .map(s -> toSnapshot(s, itemsByOrder(s.getId())));
    }

    /** Submit one answer and return immediate feedback (with explanation). */
    public AnswerOneRes answerOne(Long userId, Long sessionId, Long itemId, Integer answerIdx) {
        if (answerIdx == null || answerIdx < 1 || answerIdx > 4) {
            throw new IllegalArgumentException("answerIdx must be 1..4");
        }
        QuizSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("SESSION_NOT_FOUND"));
        if (!Objects.equals(s.getUserId(), userId)) {
            throw new org.springframework.security.access.AccessDeniedException("FORBIDDEN");
        }
        if (s.getStatus() != Status.ACTIVE) {
            throw new SessionClosedException(s.getId(), s.getStatus().name());
        }

        QuizSessionItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("ITEM_NOT_FOUND"));
        if (!Objects.equals(item.getSessionId(), s.getId())) {
            throw new IllegalArgumentException("ITEM_SESSION_MISMATCH");
        }

        // Idempotent: if already answered, just return previous result
        if (item.getUserAnswerIndex() != null) {
            boolean correctPrev = Boolean.TRUE.equals(item.getIsCorrect());
            boolean finishedPrev = finished(itemsByOrder(s.getId()));
            Integer nextOrderPrev = nextItemOrder(itemsByOrder(s.getId()));
            return new AnswerOneRes(
                    item.getId(),
                    correctPrev,
                    item.getCorrectIndex(),
                    item.getAwardedPoints(),
                    s.getTotalAwardedPoints(),
                    finishedPrev,
                    nextOrderPrev,
                    item.getExplanation()
            );
        }

        // Evaluate
        boolean correct = (answerIdx == item.getCorrectIndex());
        int awarded = correct ? POINTS_PER_CORRECT : 0;

        item.setUserAnswerIndex(answerIdx);
        item.setIsCorrect(correct);
        item.setAwardedPoints(awarded);
        itemRepo.save(item);

        s.setTotalAwardedPoints(s.getTotalAwardedPoints() + awarded);

        // If this was the last unanswered item, mark submitted
        List<QuizSessionItem> items = itemsByOrder(s.getId());
        boolean finished = finished(items);
        if (finished) {
            s.setStatus(Status.SUBMITTED);
        }
        sessionRepo.save(s);

        Integer nextOrder = nextItemOrder(items);

        return new AnswerOneRes(
                item.getId(),
                correct,
                item.getCorrectIndex(),
                awarded,
                s.getTotalAwardedPoints(),
                finished,
                nextOrder,
                item.getExplanation() // NEW: include explanation
        );
    }

    // ===== Helpers =====

    @Transactional(readOnly = true)
    public ContinuationInfo getContinuationInfo(Long sessionId) {
        QuizSession s = sessionRepo.findById(sessionId).orElse(null);
        if (s == null) return null;
        List<QuizSessionItem> items = itemsByOrder(s.getId());
        return new ContinuationInfo(
                s.getId(),
                s.getStatus().name(),
                s.getExpiresAt(),
                nextItemOrder(items),
                (int) items.stream().filter(it -> it.getUserAnswerIndex() != null).count(),
                items.size()
        );
    }

    private List<QuizSessionItem> itemsByOrder(Long sessionId) {
        return itemRepo.findBySessionIdOrderByItemOrder(sessionId);
    }

    private boolean finished(List<QuizSessionItem> items) {
        return items.stream().allMatch(it -> it.getUserAnswerIndex() != null);
    }

    private Integer nextItemOrder(List<QuizSessionItem> items) {
        return items.stream()
                .filter(it -> it.getUserAnswerIndex() == null)
                .map(QuizSessionItem::getItemOrder)
                .findFirst()
                .orElse(null);
    }

    private CreateSessionRes toSnapshot(QuizSession s, List<QuizSessionItem> items) {
        Integer next = nextItemOrder(items);
        List<CreateSessionRes.Item> view = items.stream()
                .map(it -> new CreateSessionRes.Item(
                        it.getId(),
                        it.getItemOrder(),
                        it.getPrompt(),
                        List.of(it.getChoice1(), it.getChoice2(), it.getChoice3(), it.getChoice4()),
                        it.getUserAnswerIndex()
                ))
                .collect(Collectors.toList());
        return new CreateSessionRes(
                s.getId(),
                s.getStatus().name(),
                s.getCategory(),
                s.getStartedAt(),
                s.getExpiresAt(),
                s.getTotalAwardedPoints(),
                items.size(),
                next,
                view
        );
    }

    /** Lightweight view used by exception handler to hint continuation. */
    public record ContinuationInfo(
            Long sessionId,
            String status,
            Instant expiresAt,
            Integer nextItemOrder,
            int answeredCount,
            int total
    ) {}
}
