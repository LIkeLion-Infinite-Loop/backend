package infinite_loop.sejonghack.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubcategoryResponseDto {

    private Long subcategoryId;
    private String subcategoryName;

}