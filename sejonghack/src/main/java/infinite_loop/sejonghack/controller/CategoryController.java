package infinite_loop.sejonghack.controller;

import infinite_loop.sejonghack.dto.CategoryResponseDto;
import infinite_loop.sejonghack.dto.SubcategoryResponseDto;
import infinite_loop.sejonghack.service.CategoryService;
import infinite_loop.sejonghack.service.SubcategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final SubcategoryService subcategoryService;

    // 조회: 전체 카테고리 목록 반환
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> response = categoryService.findAll()
                .stream()
                .map(c -> CategoryResponseDto.builder()
                        .categoryId(c.getCategoryId())
                        .categoryName(c.getCategoryName())
                        .build())
                .collect(toList());

        return ResponseEntity.ok(response);
    }

    // 조회: 카테고리 ID로 해당 하위 카테고리 목록 반환
    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<SubcategoryResponseDto>> getSubcategoriesByCategoryId(@PathVariable Long categoryId) {
        List<SubcategoryResponseDto> response = subcategoryService.findByCategoryId(categoryId)
                .stream()
                .map(s -> SubcategoryResponseDto.builder()
                        .subcategoryId(s.getSubcategoryId())
                        .subcategoryName(s.getSubcategoryName())
                        .build())
                .collect(toList());

        return ResponseEntity.ok(response);
    }

}