package infinite_loop.hack.controller;

import infinite_loop.hack.dto.QuizDtos.*;
import infinite_loop.hack.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService service;

    // ★ JWT에서 userId 꺼내기 (프로젝트의 Security 설정에 맞게 수정)
    private Long currentUserId(Authentication auth) {
        // 임시: 토큰 연동 전이라면 1L로 두고 테스트 → 추후 JWT 정보에서 꺼내세요.
        return 1L;
    }

    @PostMapping("/sessions")
    public ResponseEntity<CreateSessionRes> create(Authentication auth) {
        var res = service.startSession(currentUserId(auth));
        return ResponseEntity.status(201).body(res);
    }

    @PostMapping("/sessions/{sessionId}/submit")
    public ResponseEntity<SubmitRes> submit(@PathVariable Long sessionId,
                                            @RequestBody SubmitReq req,
                                            Authentication auth) {
        var res = service.submit(currentUserId(auth), sessionId, req.answers());
        return ResponseEntity.ok(res);
    }
}
