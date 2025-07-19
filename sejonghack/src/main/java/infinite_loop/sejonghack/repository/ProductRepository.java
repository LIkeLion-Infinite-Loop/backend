package infinite_loop.sejonghack.repository;

import infinite_loop.sejonghack.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 바코드로 제품 조회
    Optional<Product> findByBarcode(String barcode);

    // 제품명 키워드 포함 검색
    List<Product> findByProductNameContaining(String keyword);

}