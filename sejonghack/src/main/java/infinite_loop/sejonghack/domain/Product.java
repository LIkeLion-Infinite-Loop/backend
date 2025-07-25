package infinite_loop.sejonghack.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String productName;

    @Column(unique = true, nullable = false)
    private String barcode;

    private String category;

    private String imageUrl;

    private String description;

    @ManyToOne
    @JoinColumn(name = "barcode_guide_id")
    private BarcodeGuidePage barcodeGuide;

}