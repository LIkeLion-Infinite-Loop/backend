package infinite_loop.hack.receipt.controller;

import infinite_loop.hack.receipt.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptOcrDebugController {

    private final OcrService ocrService;

    @PostMapping(value = "/ocr-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> ocrTest(@RequestParam("file") MultipartFile file) {
        String text = ocrService.extractText(file);
        String preview = text == null ? "" : (text.length() > 500 ? text.substring(0, 500) + "..." : text);
        return Map.of(
                "length", text == null ? 0 : text.length(),
                "preview", preview
        );
    }
}