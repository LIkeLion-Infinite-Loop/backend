package infinite_loop.sejonghack.dto;

import java.time.Instant;
import java.util.List;

public class QuizDtos {
    public record CreateSessionRes(
            Long sessionId, Instant expiresAt, int numQuestions, String category,
            int attemptsLeftToday, List<Item> items
    ) { public record Item(Long itemId, int order, String prompt, List<String> choices) {} }

    public record SubmitReq(List<Answer> answers) {
        public record Answer(Long itemId, Integer answerIdx) {}
    }

    public record SubmitRes(
            Long sessionId, String category, int correctCount, int total, int awardedPoints, Instant submittedAt
    ) {}
}
