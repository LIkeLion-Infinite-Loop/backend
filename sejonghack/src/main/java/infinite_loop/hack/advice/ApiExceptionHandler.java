package infinite_loop.hack.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
        HttpStatus st = switch (e.getMessage()) {
            case "SESSION_ALREADY_ACTIVE" -> HttpStatus.CONFLICT;         // 409
            case "DAILY_LIMIT_REACHED"   -> HttpStatus.TOO_MANY_REQUESTS; // 429
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(st).body(Map.of(
                "success", false,
                "error", Map.of("code", e.getMessage(), "message", e.getMessage())
        ));
    }
}
