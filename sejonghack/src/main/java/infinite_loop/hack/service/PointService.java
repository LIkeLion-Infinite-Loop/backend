package infinite_loop.hack.service;

import infinite_loop.hack.domain.PointHistory;
import infinite_loop.hack.domain.User;
import infinite_loop.hack.dto.PointHistoryDto;
import infinite_loop.hack.repository.PointHistoryRepository;
import infinite_loop.hack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    // 현재 포인트 조회
    public Integer getCurrentPoint(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        return user.getTotalPoint();
    }

    // 포인트 적립/사용 내역 조회
    public List<PointHistoryDto> getPointHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        List<PointHistory> historyList = pointHistoryRepository.findAllByUserOrderByChangedAtDesc(user);

        return historyList.stream()
                .map(ph -> new PointHistoryDto(
                        ph.getPointAmount(),
                        ph.getReason(),
                        ph.getChangedAt()
                ))
                .toList();
    }

    // 퀴즈 포인트 적립용 간단 메서드 (없으면 추가)
    public void addPoints(Long userId, int amount, String reason) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        // 총 포인트 업데이트(필드명이 totalPoint 가정)
        user.setTotalPoint(user.getTotalPoint() + amount);
        userRepository.save(user);

        PointHistory ph = new PointHistory();
        ph.setUser(user);
        ph.setPointAmount(amount); // +적립
        ph.setReason(reason);      // 예: "QUIZ"
        pointHistoryRepository.save(ph);
    }

}
