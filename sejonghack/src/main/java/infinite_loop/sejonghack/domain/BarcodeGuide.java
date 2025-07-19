package infinite_loop.sejonghack.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class BarcodeGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String barcode;           // 제품 바코드

    @Column(nullable = false)
    private String productName;       // 제품명

    @Column(nullable = false)
    private String category;          // 분리수거 분류

    @Column(nullable = false)
    private String howToDispose;      // 처리 방법

    private String imageUrl;          // 가이드 이미지 URL (선택)

    public BarcodeGuide(String barcode, String productName, String category, String howToDispose, String imageUrl) {
        this.barcode = barcode;
        this.productName = productName;
        this.category = category;
        this.howToDispose = howToDispose;
        this.imageUrl = imageUrl;
    }
}