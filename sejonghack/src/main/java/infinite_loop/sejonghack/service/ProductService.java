package infinite_loop.sejonghack.service;

import infinite_loop.sejonghack.dto.ProductDTO;
import infinite_loop.sejonghack.dto.GuideDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    // 임의 하드코딩된 데이터 (DB 미사용)
    private final List<ProductDTO> products = List.of(
            new ProductDTO("알루미늄 캔", "금속", 1),
            new ProductDTO("고철 조각", "금속", 1),
            new ProductDTO("플라스틱 병", "플라스틱", 2),
            new ProductDTO("종이컵", "종이", 3)
    );

    public List<ProductDTO> searchProducts(String keyword) {
        return products.stream()
                .filter(p -> p.getCategory().contains(keyword) || p.getName().contains(keyword))
                .collect(Collectors.toList());
    }

    public GuideDTO getGuideById(int guideId) {
        switch (guideId) {
            case 1:
                return new GuideDTO("금속류는 이물질을 제거하고 배출하세요.", "https://example.com/images/metal.png");
            case 2:
                return new GuideDTO("플라스틱은 뚜껑을 분리해 배출하세요.", "https://example.com/images/plastic.png");
            case 3:
                return new GuideDTO("종이류는 물기에 젖지 않도록 배출하세요.", "https://example.com/images/paper.png");
            default:
                return new GuideDTO("등록되지 않은 가이드입니다.", null);
        }
    }
}
