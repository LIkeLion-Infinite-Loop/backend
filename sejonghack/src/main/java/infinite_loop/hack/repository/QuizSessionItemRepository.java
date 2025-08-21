package infinite_loop.hack.repository;

import infinite_loop.hack.domain.QuizSessionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizSessionItemRepository extends JpaRepository<QuizSessionItem, Long> {
    List<QuizSessionItem> findBySessionIdOrderByItemOrder(Long sessionId);
}
