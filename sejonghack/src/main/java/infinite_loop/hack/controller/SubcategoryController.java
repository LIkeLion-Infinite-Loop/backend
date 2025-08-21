package infinite_loop.hack.controller;

import infinite_loop.hack.domain.Subcategory;
import infinite_loop.hack.dto.SubcategoryGuideResponseDto;
import infinite_loop.hack.service.SubcategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 카테고리 ID로 서브카테고리 목록 조회
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Subcategory>> getSubcategoriesByCategory(@PathVariable Long categoryId) {
        List<Subcategory> subcategories = subcategoryService.findByCategoryId(categoryId);
        return ResponseEntity.ok(subcategories);
    }

}