package infinite_loop.sejonghack.controller;

import infinite_loop.sejonghack.domain.Subcategory;
import infinite_loop.sejonghack.dto.SubcategoryGuideResponseDto;
import infinite_loop.sejonghack.service.SubcategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subcategories")
@RequiredArgsConstructor
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    // 하위 카테고리 ID로 분리배출 가이드를 조회하는 메서드
    @GetMapping("/{subcategoryId}/guide")
    public ResponseEntity<SubcategoryGuideResponseDto> getGuideBySubcategoryId(@PathVariable Long subcategoryId) {
        Subcategory subcategory = subcategoryService.findById(subcategoryId);

        SubcategoryGuideResponseDto response = SubcategoryGuideResponseDto.builder()
                .subcategoryId(subcategory.getSubcategoryId())
                .subcategoryName(subcategory.getSubcategoryName())
                .guideContent(subcategory.getGuideContent())
                .imageUrl(subcategory.getImageUrl())
                .build();

        return ResponseEntity.ok(response);
    }

}