package infinite_loop.hack.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * DTOs for Quiz feature (one-by-one answering).
 * - Attempts are unlimited (attemptsLeftToday = -1, unlimited = true)
 * - Session expiry may be null.
 * - Each answer response now includes an explanation (feedback).
 */
public class QuizDtos {

    /** Create-session / snapshot response. */
    public record CreateSessionRes(
            Long sessionId,
            String status,
            String category,
            Instant startedAt,
            Instant expiresAt,             // may be null
            int totalAwardedPoints,
            int total,                     // number of questions
            Integer nextItemOrder,         // 1-based; null if finished
            List<Item> items               // ordered by itemOrder
    ) {
        public record Item(
                Long itemId,
                int itemOrder,             // 1..N
                String prompt,
                List<String> choices,      // 4 choices; order shown to the user
                Integer userAnswerIdx      // null if not answered yet; 1..4 otherwise
        ) {}
    }

    /** Submit-one-answer request. */
    public static final class AnswerOneReq {
        private final Long itemId;
        private final Integer answerIdx; // 1..4

        @JsonCreator
        public AnswerOneReq(
                @JsonProperty("item_id") Long itemId,
                @JsonProperty("answer_idx") Integer answerIdx
        ) {
            this.itemId = itemId;
            this.answerIdx = answerIdx;
        }
        @JsonProperty("item_id") public Long itemId() { return itemId; }
        @JsonProperty("answer_idx") public Integer answerIdx() { return answerIdx; }
    }

    /** Submit-one-answer response (now with explanation). */
    public record AnswerOneRes(
            Long itemId,
            boolean correct,
            int correctIndex,              // 1..4 (as displayed)
            int awardedPoints,             // for this item
            int totalAwardedPoints,        // accumulated in session
            boolean finished,              // true if all items answered
            Integer nextItemOrder,         // next 1-based order, null if finished
            String explanation             // NEW: feedback text
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
