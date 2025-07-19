package infinite_loop.sejonghack.service;

import infinite_loop.sejonghack.domain.Subcategory;
import infinite_loop.sejonghack.repository.SubcategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;


    // 카테고리 ID로 세부 카테고리 목록 조회
    public List<Subcategory> findByCategoryId(Long categoryId) {
        return subcategoryRepository.findByCategoryCategoryId(categoryId);
    }


    // 세부 카테고리 ID로 세부 카테고리 조회
    public Subcategory findById(Long subcategoryId) {
        return subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new NoSuchElementException("세부 카테고리를 찾을 수 없습니다."));
    }

}