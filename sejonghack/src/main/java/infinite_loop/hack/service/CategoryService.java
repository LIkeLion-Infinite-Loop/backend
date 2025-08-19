package infinite_loop.hack.service;

import infinite_loop.hack.domain.Category;
import infinite_loop.hack.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 전체 카테고리 조회
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

}