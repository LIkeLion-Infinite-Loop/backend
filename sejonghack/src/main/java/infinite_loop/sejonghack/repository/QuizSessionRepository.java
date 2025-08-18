package infinite_loop.sejonghack.repository;

import infinite_loop.sejonghack.domain.QuizSession;
import infinite_loop.sejonghack.domain.QuizSession.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    Optional<QuizSession> findFirstByUserIdAndStatus(Long userId, Status status);
    List<QuizSession> findByUserIdAndStartedAtBetween(Long userId, Instant startInclusive, Instant endExclusive);
}
