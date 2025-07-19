package infinite_loop.sejonghack.dto;

import infinite_loop.sejonghack.domain.BarcodeGuide;
import lombok.Getter;

@Getter
public class BarcodeGuideResponseDto {

    private final String productName;
    private final String barcode;
    private final String category;
    private final String howToDispose;
    private final String imageUrl;

    public BarcodeGuideResponseDto(BarcodeGuide guide) {
        this.productName = guide.getProductName();
        this.barcode = guide.getBarcode();
        this.category = guide.getCategory();
        this.howToDispose = guide.getHowToDispose();
        this.imageUrl = guide.getImageUrl();
    }
}