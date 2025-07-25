package infinite_loop.sejonghack.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외래키: 사용자 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // +적립 / -사용
    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    // 변경 사유
    @Column(length = 255)
    private String reason;

    // 변경된 시점
    @Column(name = "changed_at")
    private LocalDateTime changedAt = LocalDateTime.now();
}
