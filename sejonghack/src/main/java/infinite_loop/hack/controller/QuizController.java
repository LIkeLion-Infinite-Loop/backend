package infinite_loop.hack.controller;

import infinite_loop.hack.dto.QuizDtos.CreateSessionRes;
import infinite_loop.hack.dto.QuizDtos.AnswerOneReq;
import infinite_loop.hack.dto.QuizDtos.AnswerOneRes;
import infinite_loop.hack.security.CustomUserDetails;
import infinite_loop.hack.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService service;

    /**
     * POST /api/quiz/sessions
     * Start a new quiz session.
     * - If an ACTIVE session already exists, an ActiveSessionConflictException is thrown,
     *   and handled by ApiExceptionHandler to return 409 with continuation info.
     */
    @PostMapping("/sessions")
    public ResponseEntity<CreateSessionRes> start(Authentication authentication) {
        Long userId = currentUserId(authentication);
        CreateSessionRes res = service.startSession(userId);
        return ResponseEntity.ok(res);
    }

    /**
     * GET /api/quiz/sessions/active
     * Return the current ACTIVE session snapshot, or 404 if none.
     */
    @GetMapping("/sessions/active")
    public ResponseEntity<CreateSessionRes> getActive(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.of(service.getActiveSessionSnapshot(userId));
    }

    /**
     * GET /api/quiz/sessions/{sessionId}
     * Return a specific session snapshot (including items).
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<CreateSessionRes> getSession(Authentication authentication,
                                                       @PathVariable Long sessionId) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.of(service.getSessionSnapshot(userId, sessionId));
    }

    /**
     * POST /api/quiz/sessions/{sessionId}/answer
     * Submit a single answer for one item (one-by-one flow).
     */
    @PostMapping("/sessions/{sessionId}/answer")
    public ResponseEntity<AnswerOneRes> answerOne(Authentication authentication,
                                                  @PathVariable Long sessionId,
                                                  @RequestBody AnswerOneReq req) {
        Long userId = currentUserId(authentication);
        AnswerOneRes res = service.answerOne(userId, sessionId, req.itemId(), req.answerIdx());
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
