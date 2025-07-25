package infinite_loop.sejonghack.repository;

import infinite_loop.sejonghack.domain.ProductSearchHistory;
import infinite_loop.sejonghack.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSearchHistoryRepository extends JpaRepository<ProductSearchHistory, Long> {

    // 특정 사용자의 최근 검색 내역 (최신순, 최대 10개)
    List<ProductSearchHistory> findTop10ByUserOrderBySearchedAtDesc(User user);
}
