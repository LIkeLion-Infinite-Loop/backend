package infinite_loop.sejonghack.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponseDto {

    private Long categoryId;
    private String categoryName;

}