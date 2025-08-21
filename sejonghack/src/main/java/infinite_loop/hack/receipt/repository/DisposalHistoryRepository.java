package infinite_loop.hack.receipt.repository;

import infinite_loop.hack.receipt.domain.DisposalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisposalHistoryRepository extends JpaRepository<DisposalHistory, Long> {
    List<DisposalHistory> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}