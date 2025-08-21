package infinite_loop.hack.receipt.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "receipt_item")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ReceiptItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private Receipt receipt;

    private String name;
    private int quantity;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(length = 10)
    private String source; // AUTO | MANUAL
}