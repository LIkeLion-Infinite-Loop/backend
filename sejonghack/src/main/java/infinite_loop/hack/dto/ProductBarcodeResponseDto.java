package infinite_loop.hack.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductBarcodeResponseDto {

    private Long productId;
    private String productName;
    private String category;
    private Long barcodeGuideId;

}