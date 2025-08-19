package infinite_loop.hack.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReceiptUploadResponseDto {
    private Long receipt_id;
    private String status;  // PENDING
    private String message; // 업로드 완료. 분석을 시작합니다.
}