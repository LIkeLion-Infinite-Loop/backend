package infinite_loop.hack.advice;

import infinite_loop.hack.exception.ActiveSessionConflictException;
import infinite_loop.hack.service.QuizService;
import infinite_loop.hack.exception.SessionClosedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final QuizService quizService;

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) map.put(key, value);
    }

    private static String strOrEmpty(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    @ExceptionHandler(ActiveSessionConflictException.class)
    public ResponseEntity<Map<String, Object>> handleActiveSession(ActiveSessionConflictException e) {
        Long id = e.getSessionId();

        // 방어적으로 null 처리
        var info = (id != null) ? quizService.getContinuationInfo(id) : null;

        HttpHeaders headers = new HttpHeaders();
        if (id != null) {
            headers.add("X-Active-Session-Id", String.valueOf(id));
            headers.add("Location", "/api/quiz/sessions/" + id);
        }
        if (info != null) {
            headers.add("X-Session-Status", strOrEmpty(info.status()));
            headers.add("X-Session-Expires-At", strOrEmpty(info.expiresAt()));
            headers.add("X-Next-Item-Order", strOrEmpty(info.nextItemOrder()));
            headers.add("X-Answered-Count", strOrEmpty(info.answeredCount()));
            headers.add("X-Total-Count", strOrEmpty(info.total()));
        }

        // body 생성 (null 안전)
        Map<String, Object> session = new LinkedHashMap<>();
        if (info != null) {
            putIfNotNull(session, "id", info.sessionId());
            putIfNotNull(session, "status", info.status());
            putIfNotNull(session, "expiresAt", info.expiresAt());       // 직렬화는 Jackson이 처리
            putIfNotNull(session, "nextItemOrder", info.nextItemOrder());
            putIfNotNull(session, "answeredCount", info.answeredCount());
            putIfNotNull(session, "total", info.total());
        } else if (id != null) {
            // 최소한의 정보만
            session.put("id", id);
        }

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", "SESSION_ALREADY_ACTIVE");
        error.put("message", "An active quiz session already exists.");
        if (!session.isEmpty()) {
            error.put("session", Collections.unmodifiableMap(session));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("timestamp", Instant.now().toString());
        body.put("error", Collections.unmodifiableMap(error));

        return new ResponseEntity<>(Collections.unmodifiableMap(body), headers, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", e.getMessage() != null ? e.getMessage() : "ILLEGAL_STATE");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", Collections.unmodifiableMap(error));

        return new ResponseEntity<>(Collections.unmodifiableMap(body), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", e.getMessage() != null ? e.getMessage() : "BAD_REQUEST");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", Collections.unmodifiableMap(error));

        return new ResponseEntity<>(Collections.unmodifiableMap(body), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException e) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", "NOT_FOUND");
        if (e.getMessage() != null) {
            error.put("message", e.getMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", Collections.unmodifiableMap(error));

        return new ResponseEntity<>(Collections.unmodifiableMap(body), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SessionClosedException.class)
    public ResponseEntity<Map<String, Object>> handleSessionClosed(SessionClosedException e) {
        HttpHeaders headers = new HttpHeaders();
        if (e.getSessionId() != null) headers.add("X-Session-Id", String.valueOf(e.getSessionId()));
        if (e.getStatus() != null) headers.add("X-Session-Status", e.getStatus());
        headers.add("Location", "/api/quiz/sessions"); // 새 세션 시작 위치 안내

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", "SESSION_ALREADY_SUBMITTED");
        error.put("message", "This quiz session is already submitted. Start a new session to continue.");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", Collections.unmodifiableMap(error));

        return new ResponseEntity<>(Collections.unmodifiableMap(body), headers, HttpStatus.CONFLICT);
    }
}