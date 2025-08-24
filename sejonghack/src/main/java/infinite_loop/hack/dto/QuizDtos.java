package infinite_loop.hack.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * DTOs for Quiz feature.
 * - One-by-one answering
 * - No daily attempt limits (attemptsLeftToday = -1)
 * - No session expiry (expiresAt may be null)
 */
public class QuizDtos {

    /** Session creation / snapshot response. */
    public record CreateSessionRes(
            Long sessionId,
            Instant expiresAt,               // null when expiry disabled
            int numQuestions,
            String category,
            int attemptsLeftToday,           // -1 when unlimited
            List<Item> items
    ) {
        public record Item(Long itemId, int order, String prompt, List<String> choices) {}
    }

    /** Request: submit a single answer for one item. (tolerant parser for number/string) */
    public record AnswerOneReq(Long itemId, Integer answerIdx) {
        @JsonCreator
        public static AnswerOneReq create(
                @JsonProperty("itemId") Object itemId,
                @JsonProperty("answerIdx") Object answerIdx
        ) {
            Long iid = null;
            if (itemId != null) {
                try { iid = Long.valueOf(itemId.toString().trim()); } catch (Exception ignore) {}
            }
            Integer idx = null;
            if (answerIdx != null) {
                try { idx = Integer.valueOf(answerIdx.toString().trim()); } catch (Exception ignore) {}
            }
            return new AnswerOneReq(iid, idx);
        }
    }

    /** Response after answering one item. */
    public record AnswerOneRes(
            Long sessionId,
            Long itemId,
            boolean correct,
            int awardedPoints,
            int answeredCount,
            int total,
            boolean completed,
            Integer nextItemOrder,
            Instant submittedAt
    ) {}

    /** Legacy-style final summary (kept for compatibility if needed). */
    public record SubmitRes(
            Long sessionId,
            String category,
            int correctCount,
            int total,
            int awardedPoints,
            Instant submittedAt
    ) {}

    /** Attempts today: kept for backward compatibility (unlimited = true). */
    public record AttemptsTodayRes(
            int attemptsLeftToday,   // -1 when unlimited
            boolean unlimited,
            Window window            // may be null when unlimited
    ) {
        public record Window(
                String timezone,
                Instant resetsAt
        ) {}
    }
}
