package infinite_loop.hack.repository;

import infinite_loop.hack.domain.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    // 상위 카테고리 ID로 세부 카테고리 목록 조회
    List<Subcategory> findByCategoryCategoryId(Long categoryId);

}