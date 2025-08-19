package infinite_loop.hack.domain;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.Instant;

@Entity @Table(name="quiz_session")
@Getter @Setter
public class QuizSession {
    public enum Status { ACTIVE, SUBMITTED, EXPIRED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private String category;       // 무작위 선택
    private int numQuestions;      // 항상 3

    private Instant startedAt = Instant.now();
    private Instant expiresAt;     // 시작 + 10분

    private int totalAwardedPoints;

    public boolean isExpired() { return expiresAt != null && Instant.now().isAfter(expiresAt); }
}
