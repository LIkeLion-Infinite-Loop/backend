package infinite_loop.sejonghack.service;

import infinite_loop.sejonghack.domain.PointHistory;
import infinite_loop.sejonghack.domain.User;
import infinite_loop.sejonghack.dto.PointHistoryDto;
import infinite_loop.sejonghack.repository.PointHistoryRepository;
import infinite_loop.sejonghack.repository.UserRepository;
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
}
