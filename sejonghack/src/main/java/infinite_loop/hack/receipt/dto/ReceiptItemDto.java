package infinite_loop.hack.receipt.dto;

import lombok.*;
import infinite_loop.hack.receipt.domain.Category;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReceiptItemDto {
    private Long item_id;
    private String name;
    private int quantity;
    private Category category;
    private String guide_page_url; // 프론트 연결용
}