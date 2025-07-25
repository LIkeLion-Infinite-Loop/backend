package infinite_loop.sejonghack.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BarcodeGuidePageResponseDto {

    private Long guideId;
    private String guideContent;
    private String imageUrl;

}