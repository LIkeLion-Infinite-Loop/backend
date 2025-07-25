package infinite_loop.sejonghack.repository;

import infinite_loop.sejonghack.domain.PointHistory;
import infinite_loop.sejonghack.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 사용자별 포인트 내역 조회 (최신순)
    List<PointHistory> findAllByUserOrderByChangedAtDesc(User user);
}
