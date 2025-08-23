package infinite_loop.hack.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import infinite_loop.hack.exception.ActiveSessionConflictException;

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

    // ★ 추가: 활성 세션 충돌 시 헤더 + 세션ID 바디로 내려주기
    @ExceptionHandler(ActiveSessionConflictException.class)
    public ResponseEntity<Map<String, Object>> handleActiveSession(ActiveSessionConflictException e) {
        Long id = e.getSessionId();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Active-Session-Id", String.valueOf(id));
        headers.add("Location", "/api/quiz/sessions/" + id);

        return new ResponseEntity<>(
                Map.of(
                        "success", false,
                        "error", Map.of("code", "SESSION_ALREADY_ACTIVE", "sessionId", id)
                ),
                headers,
                HttpStatus.CONFLICT
        );
    }
}
