package infinite_loop.sejonghack.search_test;

import infinite_loop.sejonghack.controller.ProductController;
import infinite_loop.sejonghack.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import infinite_loop.sejonghack.dto.ProductDTO;

@WebMvcTest(ProductController.class)
public class CategorySearchTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("카테고리 키워드로 검색 성공")
    void testSearchByCategoryKeyword() throws Exception {
        // Given
        String keyword = "금속";
        List<ProductDTO> mockList = List.of(
                new ProductDTO("알루미늄 캔", "금속", 1),
                new ProductDTO("고철 조각", "금속", 1)
        );

        when(productService.searchProducts(keyword)).thenReturn(mockList);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("알루미늄 캔"))
                .andExpect(jsonPath("$[0].category").value("금속"));
    }

    @Test
    @DisplayName("가이드 ID로 분리배출 가이드 조회")
    void testGetGuideById() throws Exception {
        // Given
        when(productService.getGuideById(1)).thenReturn(
                new infinite_loop.sejonghack.dto.GuideDTO("금속류는 이물질을 제거하고 배출하세요.", "https://example.com/images/metal.png")
        );

        // When & Then
        mockMvc.perform(get("/api/guides/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guideText").value("금속류는 이물질을 제거하고 배출하세요."))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/images/metal.png"));
    }
}
