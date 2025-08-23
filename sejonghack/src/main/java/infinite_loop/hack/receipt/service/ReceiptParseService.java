package infinite_loop.hack.receipt.service;

import infinite_loop.hack.receipt.domain.Category;
import infinite_loop.hack.receipt.domain.Receipt;
import infinite_loop.hack.receipt.domain.ReceiptItem;
import infinite_loop.hack.receipt.domain.ReceiptStatus;
import infinite_loop.hack.receipt.dto.ParsedItemDto;
import infinite_loop.hack.receipt.repository.ReceiptItemRepository;
import infinite_loop.hack.receipt.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptParseService {

    private final GptService gptService;
    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;

    @Async
    @Transactional
    public void parseAsync(Long receiptId) {
        try {
            Receipt r = receiptRepository.findById(receiptId).orElseThrow();
            String raw = r.getRawText();
            if (raw == null || raw.isBlank()) {
                throw new IllegalStateException("RAW 텍스트 없음: receiptId=" + receiptId);
            }

            List<ParsedItemDto> items = gptService.parseReceipt(raw);

            int saved = 0;
            for (ParsedItemDto it : items) {
                if (it == null || it.getName() == null || it.getName().isBlank()) continue;

                Category cat;
                try {
                    cat = Category.valueOf(it.getCategory().trim().toUpperCase());
                } catch (Exception e) {
                    cat = Category.ETC;
                }

                ReceiptItem entity = ReceiptItem.builder()
                        .receipt(r)
                        .name(it.getName())
                        .quantity(it.getQuantity() == null || it.getQuantity() <= 0 ? 1 : it.getQuantity())
                        .category(cat)
                        .source("GPT")
                        .build();

                receiptItemRepository.save(entity);
                saved++;
            }

            r.setStatus(saved > 0 ? ReceiptStatus.PARSED : ReceiptStatus.FAILED);
            log.info("[PARSE] 완료 receiptId={} saved={}", receiptId, saved);

        } catch (Exception e) {
            log.error("[PARSE] 실패 receiptId={}", receiptId, e);
            Receipt r = receiptRepository.findById(receiptId).orElse(null);
            if (r != null) {
                r.setStatus(ReceiptStatus.FAILED);
            }
        }
    }
}