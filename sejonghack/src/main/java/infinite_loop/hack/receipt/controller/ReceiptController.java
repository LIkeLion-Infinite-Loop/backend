package infinite_loop.hack.receipt.controller;

import infinite_loop.hack.receipt.dto.*;
import infinite_loop.hack.receipt.service.ReceiptService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    /** (A) form-data 업로드: key=파일필드 (예: file) */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptUploadResponseDto> uploadReceipt(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestPart MultipartFile file
    ) {
        ReceiptUploadResponseDto resp = receiptService.upload(userId, file);
        return ResponseEntity.ok(resp);
    }

    /** (B) 같은 경로에서 raw 이미지/바이너리도 지원 (key 없이 파일만 전송) */
    @PostMapping(
            value = "/upload",
            consumes = {
                    MediaType.IMAGE_JPEG_VALUE,
                    MediaType.IMAGE_PNG_VALUE,
                    MediaType.APPLICATION_OCTET_STREAM_VALUE
            }
    )
    public ResponseEntity<ReceiptUploadResponseDto> uploadReceiptRaw(
            @AuthenticationPrincipal(expression = "id") Long userId,
            HttpServletRequest request
    ) throws IOException {
        byte[] bytes = request.getInputStream().readAllBytes();
        if (bytes.length == 0) {
            return ResponseEntity.badRequest().body(
                    new ReceiptUploadResponseDto(null, "FAILED", "빈 바디입니다. 파일이 전송되지 않았습니다.")
            );
        }
        ReceiptUploadResponseDto resp = receiptService.uploadFromBytes(userId, bytes);
        return ResponseEntity.ok(resp);
    }

    /** (옵션) 수동 파싱 트리거 */
    @PostMapping("/{receiptId}/parse")
    public ResponseEntity<String> parseReceipt(@PathVariable Long receiptId) {
        receiptService.parse(receiptId);
        return ResponseEntity.ok("영수증 분석 시작됨 (비동기)");
    }

    @GetMapping("/{receiptId}/status")
    public ResponseEntity<ReceiptStatusResponseDto> getStatus(@PathVariable Long receiptId) {
        return ResponseEntity.ok(receiptService.getStatus(receiptId));
    }

    @GetMapping("/{receiptId}/items")
    public ResponseEntity<ReceiptItemsResponseDto> getItems(@PathVariable Long receiptId) {
        return ResponseEntity.ok(receiptService.getItems(receiptId));
    }

    @PutMapping("/{receiptId}/items/{itemId}")
    public ResponseEntity<Void> modifyItem(
            @PathVariable Long receiptId,
            @PathVariable Long itemId,
            @RequestBody ItemModifyRequest req
    ) {
        receiptService.modifyItem(receiptId, itemId, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{receiptId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long receiptId,
            @PathVariable Long itemId
    ) {
        receiptService.deleteItem(receiptId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{receiptId}/items")
    public ResponseEntity<Long> addItem(
            @PathVariable Long receiptId,
            @RequestBody ItemCreateRequest req
    ) {
        Long id = receiptService.addItem(receiptId, req);
        return ResponseEntity.ok(id);
    }

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