package infinite_loop.hack.receipt.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ParsedItemDto {
    private String name;
    private Integer quantity;   // 없으면 1로 처리
    private Integer price;      // 옵션
    private String category;    // PLASTIC | PAPER | GLASS | METAL | VINYL | FOOD | ETC
}