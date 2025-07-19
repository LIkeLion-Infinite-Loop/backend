package infinite_loop.sejonghack.controller;

import infinite_loop.sejonghack.dto.ProductDTO;
import infinite_loop.sejonghack.dto.GuideDTO;
import infinite_loop.sejonghack.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // /api/products/search?keyword=금속
    @GetMapping("/products/search")
    public List<ProductDTO> searchProducts(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

    // /api/guides/1
    @GetMapping("/guides/{guideId}")
    public GuideDTO getGuide(@PathVariable int guideId) {
        return productService.getGuideById(guideId);
    }
}
