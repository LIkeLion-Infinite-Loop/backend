package infinite_loop.hack.receipt.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "disposal_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DisposalHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String name;
    private int quantity;

    @Enumerated(EnumType.STRING)
    private Category category;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}