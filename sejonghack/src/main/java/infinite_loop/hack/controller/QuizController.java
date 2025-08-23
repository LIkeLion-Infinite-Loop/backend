package infinite_loop.hack.controller;

import infinite_loop.hack.dto.QuizDtos.CreateSessionRes;
import infinite_loop.hack.dto.QuizDtos.SubmitReq;
import infinite_loop.hack.dto.QuizDtos.SubmitRes;
import infinite_loop.hack.security.CustomUserDetails;
import infinite_loop.hack.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService service;

    /**
     * 퀴즈 세션 생성
     * POST /api/quiz/sessions
     */
    @PostMapping("/sessions")
    public ResponseEntity<CreateSessionRes> create(Authentication auth) {
        Long userId = currentUserId(auth);
        CreateSessionRes res = service.startSession(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * 정답 제출
     * POST /api/quiz/sessions/{sessionId}/submit
     * Body: { "answers": [ { "itemId": 101, "answerIdx": 1 }, ... ] }
     *  - answerIdx는 1~4 범위
     */
    @PostMapping("/sessions/{sessionId}/submit")
    public ResponseEntity<SubmitRes> submit(@PathVariable Long sessionId,
                                            @RequestBody SubmitReq req,
                                            Authentication auth) {
        Long userId = currentUserId(auth);
        SubmitRes res = service.submit(userId, sessionId, req.answers());
        return ResponseEntity.ok(res);
    }

    // 활성 세션 조회 (재접속용)
    @GetMapping("/sessions/active")
    public ResponseEntity<CreateSessionRes> getActive(Authentication auth) {
        Long userId = currentUserId(auth);
        CreateSessionRes res = service.getActive(userId);
        if (res == null) return ResponseEntity.noContent().build(); // 204
        return ResponseEntity.ok(res);
    }

    // 특정 세션 ID로 조회 (409 응답의 Location 헤더 따라가기)
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<CreateSessionRes> getById(@PathVariable Long sessionId, Authentication auth) {
        Long userId = currentUserId(auth);
        CreateSessionRes res = service.getById(userId, sessionId);
        if (res == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(res);
    }

    /**
     * JWT에서 현재 사용자 id 추출
     */
    private Long currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new org.springframework.security.access.AccessDeniedException("UNAUTHENTICATED");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud && cud.getId() != null) {
            return cud.getId();
        }
        throw new org.springframework.security.access.AccessDeniedException("UNAUTHENTICATED");
    }
}
