package infinite_loop.hack.advice;

import infinite_loop.hack.exception.ActiveSessionConflictException;
import infinite_loop.hack.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Unified API exception handling.
 * - 409 CONFLICT for ActiveSessionConflictException, with continuation headers/body.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final QuizService quizService;

    @ExceptionHandler(ActiveSessionConflictException.class)
    public ResponseEntity<Map<String, Object>> handleActiveSession(ActiveSessionConflictException e) {
        Long id = e.getSessionId();
        var info = quizService.getContinuationInfo(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Active-Session-Id", String.valueOf(id));
        headers.add("X-Session-Status", info.status());
        headers.add("X-Session-Expires-At", info.expiresAt() != null ? info.expiresAt().toString() : "");
        headers.add("X-Next-Item-Order", info.nextItemOrder() != null ? String.valueOf(info.nextItemOrder()) : "");
        headers.add("X-Answered-Count", String.valueOf(info.answeredCount()));
        headers.add("X-Total-Count", String.valueOf(info.total()));
        headers.add("Location", "/api/quiz/sessions/" + id);

        Map<String, Object> body = Map.of(
                "success", false,
                "error", Map.of(
                        "code", "SESSION_ALREADY_ACTIVE",
                        "message", "An active quiz session already exists.",
                        "session", Map.of(
                                "id", info.sessionId(),
                                "status", info.status(),
                                "expiresAt", info.expiresAt(),
                                "nextItemOrder", info.nextItemOrder(),
                                "answeredCount", info.answeredCount(),
                                "total", info.total()
                        )
                )
        );

        return new ResponseEntity<>(body, headers, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
        Map<String, Object> body = Map.of(
                "success", false,
                "error", Map.of(
                        "code", e.getMessage() != null ? e.getMessage() : "ILLEGAL_STATE"
                )
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, Object> body = Map.of(
                "success", false,
                "error", Map.of(
                        "code", e.getMessage() != null ? e.getMessage() : "BAD_REQUEST"
                )
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
