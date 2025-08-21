package infinite_loop.hack.controller;

import infinite_loop.hack.domain.Product;
import infinite_loop.hack.dto.ProductBarcodeResponseDto;
import infinite_loop.hack.dto.ProductDTO;
import infinite_loop.hack.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 바코드로 제품 조회
    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ProductBarcodeResponseDto> getProductByBarcode(@PathVariable String barcode) {
        Product product = productService.findByBarcode(barcode);

        ProductBarcodeResponseDto response = ProductBarcodeResponseDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .category(product.getCategory())
                .barcodeGuideId(product.getBarcodeGuide() != null ? product.getBarcodeGuide().getGuideId() : null)
                .build();

        return ResponseEntity.ok(response);
    }

    // 제품 키워드로 조회
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String keyword) {
        List<ProductDTO> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }
}