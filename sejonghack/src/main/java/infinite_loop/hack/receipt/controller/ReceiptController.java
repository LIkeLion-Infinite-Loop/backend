package infinite_loop.hack.receipt.controller;

import infinite_loop.hack.receipt.dto.*;
import infinite_loop.hack.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    /** 1) 영수증 업로드: OCR 실행 + Receipt(PENDING) 저장 */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptUploadResponseDto> uploadReceipt(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestPart("file") MultipartFile file
    ) {
        ReceiptUploadResponseDto resp = receiptService.upload(userId, file);
        return ResponseEntity.ok(resp);
    }

    /** 2) OCR 결과 파싱 요청: GPT 호출 → 상태 반환(PARSED/FAILED) */
    @PostMapping("/{receiptId}/parse")
    public ResponseEntity<ReceiptStatusResponseDto> parseReceipt(@PathVariable Long receiptId) {
        receiptService.parse(receiptId);
        return ResponseEntity.ok(receiptService.getStatus(receiptId));
    }

    /** 3) 상태 조회 */
    @GetMapping("/{receiptId}/status")
    public ResponseEntity<ReceiptStatusResponseDto> getStatus(@PathVariable Long receiptId) {
        return ResponseEntity.ok(receiptService.getStatus(receiptId));
    }

    /** 4) 아이템 목록 조회 */
    @GetMapping("/{receiptId}/items")
    public ResponseEntity<ReceiptItemsResponseDto> getItems(@PathVariable Long receiptId) {
        return ResponseEntity.ok(receiptService.getItems(receiptId));
    }

    /** 5) 아이템 수정 */
    @PutMapping("/{receiptId}/items/{itemId}")
    public ResponseEntity<Void> modifyItem(
            @PathVariable Long receiptId,
            @PathVariable Long itemId,
            @RequestBody ItemModifyRequest req
    ) {
        receiptService.modifyItem(receiptId, itemId, req);
        return ResponseEntity.noContent().build();
    }

    /** 6) 아이템 삭제 */
    @DeleteMapping("/{receiptId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long receiptId,
            @PathVariable Long itemId
    ) {
        receiptService.deleteItem(receiptId, itemId);
        return ResponseEntity.noContent().build();
    }

    /** 7) 아이템 수동 추가 */
    @PostMapping("/{receiptId}/items")
    public ResponseEntity<Long> addItem(
            @PathVariable Long receiptId,
            @RequestBody ItemCreateRequest req
    ) {
        Long id = receiptService.addItem(receiptId, req);
        return ResponseEntity.ok(id);
    }

    /** 8) 저장하기(확정) */
    @PostMapping("/{receiptId}/confirm")
    public ResponseEntity<Map<String, Object>> confirm(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long receiptId,
            @RequestBody ConfirmRequest req
    ) {
        Map<String, Object> resp = receiptService.confirm(userId, receiptId, req);
        return ResponseEntity.ok(resp);
    }
}