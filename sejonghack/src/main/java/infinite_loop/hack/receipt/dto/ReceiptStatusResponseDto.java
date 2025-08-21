package infinite_loop.hack.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReceiptStatusResponseDto {
    private Long receipt_id;
    private String status; // PENDING | PARSED | FAILED | CONFIRMED
}