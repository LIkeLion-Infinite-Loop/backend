package infinite_loop.hack.controller;

import infinite_loop.hack.dto.QuizDtos.CreateSessionRes;
import infinite_loop.hack.dto.QuizDtos.AnswerOneReq;
import infinite_loop.hack.dto.QuizDtos.AnswerOneRes;
import infinite_loop.hack.dto.QuizDtos.AttemptsTodayRes;
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

    /** POST /api/quiz/sessions — start a new quiz session (409 if one active). */
    @PostMapping("/sessions")
    public ResponseEntity<CreateSessionRes> start(Authentication authentication) {
        Long userId = currentUserId(authentication);
        CreateSessionRes res = service.startSession(userId);
        return ResponseEntity.ok(res);
    }

    /** GET /api/quiz/sessions/active — current ACTIVE session snapshot, or 404. */
    @GetMapping("/sessions/active")
    public ResponseEntity<CreateSessionRes> getActive(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.of(service.getActiveSessionSnapshot(userId));
    }

    /** GET /api/quiz/sessions/{sessionId} — specific session snapshot. */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<CreateSessionRes> getSession(Authentication authentication,
                                                       @PathVariable Long sessionId) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.of(service.getSessionSnapshot(userId, sessionId));
    }

    /** POST /api/quiz/sessions/{sessionId}/answer — submit single answer. */
    @PostMapping("/sessions/{sessionId}/answer")
    public ResponseEntity<AnswerOneRes> answerOne(Authentication authentication,
                                                  @PathVariable Long sessionId,
                                                  @RequestBody AnswerOneReq req) {
        Long userId = currentUserId(authentication);
        AnswerOneRes res = service.answerOne(userId, sessionId, req.itemId(), req.answerIdx());
        return ResponseEntity.ok(res);
    }

    /** NEW: GET /api/quiz/attempts/today — remaining attempts today (KST). */
    @GetMapping("/attempts/today")
    public ResponseEntity<AttemptsTodayRes> attemptsToday(Authentication authentication) {
        Long userId = currentUserId(authentication);
        AttemptsTodayRes res = service.getAttemptsToday(userId);
        return ResponseEntity.ok(res);
    }

    /** Extract user id from Authentication. */
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
