package infinite_loop.sejonghack.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubcategoryGuideResponseDto {

    private Long subcategoryId;
    private String subcategoryName;
    private String guideContent;
    private String imageUrl;

}