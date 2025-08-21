package infinite_loop.hack.receipt.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReceiptItemsResponseDto {
    private Long receipt_id;
    private Map<String, Integer> counts_by_category;
    private List<ReceiptItemDto> items;
}