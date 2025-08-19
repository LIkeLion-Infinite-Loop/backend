package infinite_loop.hack.service;

import infinite_loop.hack.domain.Product;
import infinite_loop.hack.dto.GuideDTO;
import infinite_loop.hack.dto.ProductDTO;
import infinite_loop.hack.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 바코드로 제품 조회
    public Product findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new NoSuchElementException("해당 바코드의 제품을 찾을 수 없습니다."));
    }

    // 키워드로 제품 검색     
    public List<ProductDTO> searchProducts(String keyword) {
        List<Product> products = productRepository.findByProductNameContaining(keyword);

        return products.stream()
                .map(product -> new ProductDTO(
                        product.getProductName(),
                        product.getCategory(),
                        product.getProductId()
                )).collect(Collectors.toList());
    }

    // GuideDTO를 반환하는 메서드 추가
    public GuideDTO getGuideById(int id) {
        // 테스트 목적의 임시 반환
        return new GuideDTO("가이드 내용 예시", "https://example.com/images/sample.png");
    }


}