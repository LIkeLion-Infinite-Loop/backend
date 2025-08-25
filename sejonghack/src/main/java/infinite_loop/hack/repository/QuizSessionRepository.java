package infinite_loop.hack.repository;

import infinite_loop.hack.domain.QuizSession;
import infinite_loop.hack.domain.QuizSession.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {

    Optional<QuizSession> findFirstByUserIdAndStatus(Long userId, Status status);

    List<QuizSession> findByUserIdAndStartedAtBetween(Long userId, Instant startInclusive, Instant endExclusive);

    // NEW: 정확/경량 카운트
    int countByUserIdAndStartedAtBetween(Long userId, Instant startInclusive, Instant endExclusive);
}
