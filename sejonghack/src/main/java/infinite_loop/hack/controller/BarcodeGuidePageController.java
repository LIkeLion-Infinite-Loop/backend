package infinite_loop.hack.controller;

import infinite_loop.hack.domain.BarcodeGuidePage;
import infinite_loop.hack.dto.BarcodeGuidePageResponseDto;
import infinite_loop.hack.service.BarcodeGuidePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/guides")
@RequiredArgsConstructor
public class BarcodeGuidePageController {

    private final BarcodeGuidePageService barcodeGuidePageService;

    // 가이드 ID로 가이드 조회
    @GetMapping("/{guideId}")
    public ResponseEntity<BarcodeGuidePageResponseDto> getGuideById(@PathVariable Long guideId) {
        BarcodeGuidePage guide = barcodeGuidePageService.findById(guideId);

        BarcodeGuidePageResponseDto response = BarcodeGuidePageResponseDto.builder()
                .guideId(guide.getGuideId())
                .guideContent(guide.getGuideContent())
                .imageUrl(guide.getImageUrl())
                .build();

        return ResponseEntity.ok(response);
    }

}