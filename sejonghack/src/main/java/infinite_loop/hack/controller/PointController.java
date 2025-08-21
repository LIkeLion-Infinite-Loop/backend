package infinite_loop.hack.controller;

import infinite_loop.hack.dto.PointHistoryDto;
import infinite_loop.hack.security.CustomUserDetails;
import infinite_loop.hack.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    /**
     * 내 보유 포인트
     * GET /api/points/me
     */
    @GetMapping("/me")
    public ResponseEntity<Integer> getCurrentPoint(@AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(pointService.getCurrentPoint(me.getId()));
    }

    /**
     * 내 포인트 내역
     * GET /api/points/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<PointHistoryDto>> getPointHistory(@AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(pointService.getPointHistory(me.getId()));
    }
}
