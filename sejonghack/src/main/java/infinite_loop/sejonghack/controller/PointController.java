package infinite_loop.sejonghack.controller;

import infinite_loop.sejonghack.dto.PointHistoryDto;
import infinite_loop.sejonghack.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    // 현재 포인트 조회
    @GetMapping("/me")
    public ResponseEntity<Integer> getCurrentPoint() {
        Long userId = 1L; // 추후 JWT에서 대체
        return ResponseEntity.ok(pointService.getCurrentPoint(userId));
    }

    // 포인트 내역 조회
    @GetMapping("/history")
    public ResponseEntity<List<PointHistoryDto>> getPointHistory() {
        Long userId = 1L; // 추후 JWT에서 대체
        return ResponseEntity.ok(pointService.getPointHistory(userId));
    }
}
