package infinite_loop.hack.receipt.dto;

import infinite_loop.hack.receipt.domain.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemModifyRequest {
    private String name;
    private Integer quantity;
    private Category category;
}