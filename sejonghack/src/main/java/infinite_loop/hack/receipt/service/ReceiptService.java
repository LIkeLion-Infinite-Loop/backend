package infinite_loop.hack.receipt.service;

import infinite_loop.hack.receipt.domain.*;
import infinite_loop.hack.receipt.dto.*;
import infinite_loop.hack.receipt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);

    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final DisposalHistoryRepository disposalHistoryRepository;
    private final OcrService ocrService;
    private final GptService gptService;  // ✅ GPT 파서 주입

    /** 업로드: 영수증 엔티티 생성(PENDING) + OCR 실행 */
    @Transactional
    public ReceiptUploadResponseDto upload(Long userId, MultipartFile file) {
        String rawText = ocrService.extractText(file);

        Receipt receipt = Receipt.builder()
                .userId(userId)
                .status(ReceiptStatus.PENDING)
                .rawText(rawText)
                .build();
        receiptRepository.save(receipt);

        return new ReceiptUploadResponseDto(
                receipt.getId(),
                receipt.getStatus().name(),
                "업로드 완료. 분석을 시작합니다."
        );
    }

    @Transactional
    public ReceiptUploadResponseDto uploadFromBytes(Long userId, byte[] bytes) {
        String rawText = ocrService.extractText(bytes);
        return savePending(userId, rawText);
    }

    private ReceiptUploadResponseDto savePending(Long userId, String rawText) {
        Receipt receipt = Receipt.builder()
                .userId(userId)
                .status(ReceiptStatus.PENDING)
                .rawText(rawText)
                .build();
        receiptRepository.save(receipt);

        return new ReceiptUploadResponseDto(
                receipt.getId(),
                receipt.getStatus().name(),
                "업로드 완료. 분석을 시작합니다."
        );
    }

    /** 상태 조회 */
    @Transactional(readOnly = true)
    public ReceiptStatusResponseDto getStatus(Long receiptId) {
        Receipt r = getReceiptOrThrow(receiptId);
        return new ReceiptStatusResponseDto(r.getId(), r.getStatus().name());
    }

    /** 아이템 목록 조회 */
    @Transactional(readOnly = true)
    public ReceiptItemsResponseDto getItems(Long receiptId) {
        Receipt r = getReceiptOrThrow(receiptId);
        List<ReceiptItem> items = receiptItemRepository.findAllByReceiptIdOrderByIdAsc(receiptId);

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ReceiptItem it : items) {
            String key = it.getCategory().name();
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }

        List<ReceiptItemDto> itemDtos = items.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ReceiptItemsResponseDto.builder()
                .receipt_id(r.getId())
                .counts_by_category(counts)
                .items(itemDtos)
                .build();
    }

    /** OCR 결과 파싱 → ReceiptItem 생성 (GPT 연동) */
    @Transactional
    public void parse(Long receiptId) {
        Receipt r = getReceiptOrThrow(receiptId);

        if (r.getRawText() == null || r.getRawText().isBlank()) {
            log.warn("[PARSE] rawText 비어있음. receiptId={}", receiptId);
            r.setStatus(ReceiptStatus.FAILED);
            return;
        }

        try {
            List<ParsedItemDto> parsed = gptService.parseReceipt(r.getRawText());
            int saved = 0;

            for (ParsedItemDto dto : parsed) {
                if (dto.getName() == null || dto.getName().isBlank()) continue;

                int qty = (dto.getQuantity() == null || dto.getQuantity() <= 0) ? 1 : dto.getQuantity();
                Category category = safeCategory(dto.getCategory());

                ReceiptItem item = ReceiptItem.builder()
                        .receipt(r)
                        .name(dto.getName())
                        .quantity(qty)
                        .category(category)
                        .source("GPT")
                        .build();
                receiptItemRepository.save(item);
                saved++;
            }

            r.setStatus(saved > 0 ? ReceiptStatus.PARSED : ReceiptStatus.FAILED);
            if (saved == 0) {
                log.warn("[PARSE] 저장된 항목이 0개. GPT 응답 점검 필요. receiptId={}", receiptId);
            }
        } catch (Exception e) {
            log.error("[PARSE] 예외 발생. FAILED 전환. receiptId={}", receiptId, e);
            r.setStatus(ReceiptStatus.FAILED);
        }
    }

    /** 항목 수정 */
    @Transactional
    public void modifyItem(Long receiptId, Long itemId, ItemModifyRequest req) {
        Receipt r = getReceiptOrThrow(receiptId);
        ReceiptItem item = receiptItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("ITEM_NOT_FOUND"));

        if (!item.getReceipt().getId().equals(r.getId())) {
            throw new IllegalStateException("ITEM_NOT_IN_RECEIPT");
        }

        if (req.getName() != null) item.setName(req.getName());
        if (req.getQuantity() != null) item.setQuantity(req.getQuantity());
        if (req.getCategory() != null) item.setCategory(req.getCategory());
    }

    /** 항목 삭제 */
    @Transactional
    public void deleteItem(Long receiptId, Long itemId) {
        Receipt r = getReceiptOrThrow(receiptId);
        ReceiptItem item = receiptItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("ITEM_NOT_FOUND"));

        if (!item.getReceipt().getId().equals(r.getId())) {
            throw new IllegalStateException("ITEM_NOT_IN_RECEIPT");
        }
        receiptItemRepository.delete(item);
    }

    /** 항목 수동 추가 */
    @Transactional
    public Long addItem(Long receiptId, ItemCreateRequest req) {
        Receipt r = getReceiptOrThrow(receiptId);
        ReceiptItem item = ReceiptItem.builder()
                .receipt(r)
                .name(req.getName())
                .quantity(req.getQuantity() == null ? 1 : req.getQuantity())
                .category(req.getCategory())
                .source("MANUAL")
                .build();
        receiptItemRepository.save(item);
        return item.getId();
    }

    /** 저장하기(확정) → DisposalHistory로 이동/복사 */
    @Transactional
    public Map<String, Object> confirm(Long userId, Long receiptId, ConfirmRequest req) {
        Receipt r = getReceiptOrThrow(receiptId);
        if (!Objects.equals(r.getUserId(), userId)) {
            throw new IllegalStateException("FORBIDDEN");
        }

        List<ReceiptItem> items = receiptItemRepository.findAllByReceiptIdOrderByIdAsc(receiptId);
        Set<Long> selected = req.getSelected_item_ids() == null
                ? items.stream().map(ReceiptItem::getId).collect(Collectors.toSet())
                : new HashSet<>(req.getSelected_item_ids());

        int count = 0;
        for (ReceiptItem it : items) {
            if (!selected.contains(it.getId())) continue;
            disposalHistoryRepository.save(DisposalHistory.builder()
                    .userId(userId)
                    .name(it.getName())
                    .quantity(it.getQuantity())
                    .category(it.getCategory())
                    .build());
            count++;
        }
        r.setStatus(ReceiptStatus.CONFIRMED);

        Map<String, Integer> countsByCategory = new LinkedHashMap<>();
        for (ReceiptItem it : items) {
            if (!selected.contains(it.getId())) continue;
            String key = it.getCategory().name();
            countsByCategory.put(key, countsByCategory.getOrDefault(key, 0) + 1);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total_items", count);
        summary.put("counts_by_category", countsByCategory);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("message", "확정 완료");
        resp.put("summary", summary);
        return resp;
    }

    // ===== helpers =====

    private Receipt getReceiptOrThrow(Long id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("RECEIPT_NOT_FOUND"));
    }

    private String guideUrl(Category c) {
        return "/guides/" + c.name().toLowerCase();
    }

    private ReceiptItemDto toDto(ReceiptItem it) {
        return ReceiptItemDto.builder()
                .item_id(it.getId())
                .name(it.getName())
                .quantity(it.getQuantity())
                .category(it.getCategory())
                .guide_page_url(guideUrl(it.getCategory()))
                .build();
    }

    private Category safeCategory(String s) {
        if (s == null) return Category.ETC;
        try { return Category.valueOf(s.trim().toUpperCase()); }
        catch (Exception e) { return Category.ETC; }
    }
}