package infinite_loop.hack.receipt.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receipt")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Receipt {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private ReceiptStatus status;

    @Column(columnDefinition = "TEXT")
    private String rawText;

    private LocalDateTime uploadedAt;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReceiptItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) uploadedAt = LocalDateTime.now();
        if (status == null) status = ReceiptStatus.PENDING;
    }
}