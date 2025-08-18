package infinite_loop.sejonghack.repository;

import infinite_loop.sejonghack.domain.QuizSessionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizSessionItemRepository extends JpaRepository<QuizSessionItem, Long> {
    List<QuizSessionItem> findBySessionIdOrderByItemOrder(Long sessionId);
}
