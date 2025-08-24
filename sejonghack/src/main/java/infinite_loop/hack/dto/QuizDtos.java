package infinite_loop.hack.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * DTOs for Quiz feature.
 * Now supports one-by-one answering instead of submitting all 3 at once.
 */
public class QuizDtos {

    /** Session creation response (also reused for fetching a session snapshot). */
    public record CreateSessionRes(
            Long sessionId,
            Instant expiresAt,
            int numQuestions,
            String category,
            int attemptsLeftToday,
            List<Item> items
    ) {
        public record Item(Long itemId, int order, String prompt, List<String> choices) {}
    }

    /** Request: submit a single answer for one item. */
    public record AnswerOneReq(Long itemId, Integer answerIdx) {
        @JsonCreator
        public static AnswerOneReq create(
                @JsonProperty("itemId") Object itemId,
                @JsonProperty("answerIdx") Object answerIdx
                // (선택) @JsonProperty("answerIndex") Object answerIndex 로도 추가 가능
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

    /**
     * Response after answering one item.
     * If completed == true, this represents the final summary for the session.
     */
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
}
