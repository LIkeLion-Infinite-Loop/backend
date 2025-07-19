package infinite_loop.sejonghack.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "barcode_guide_page")
@Getter
@Setter
@NoArgsConstructor
public class BarcodeGuidePage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guideId;

    @Column(nullable = false)
    private String guideContent;

    private String imageUrl;

}