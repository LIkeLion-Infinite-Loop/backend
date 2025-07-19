package infinite_loop.sejonghack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductDTO {
    private String name;
    private String category;
    private long guideId;
}