package infinite_loop.sejonghack;

import infinite_loop.sejonghack.controller.ProductController;
import infinite_loop.sejonghack.dto.GuideDTO;
import infinite_loop.sejonghack.dto.ProductDTO;
import infinite_loop.sejonghack.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(CategorySearchTest.MockConfig.class)  // 추가
public class CategorySearchTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;  // MockBean 대신 직접 주입

    @TestConfiguration  // 빈 재정의
    static class MockConfig {
        @Bean
        @Primary
        public ProductService productService() {
            return mock(ProductService.class);
        }
    }

    @Test
    @DisplayName("카테고리 키워드로 검색 성공")
    void testSearchByCategoryKeyword() throws Exception {
        String keyword = "금속";
        List<ProductDTO> mockList = List.of(
                new ProductDTO("알루미늄 캔", "금속", 1),
                new ProductDTO("고철 조각", "금속", 1)
        );

        when(productService.searchProducts(keyword)).thenReturn(mockList);

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
        when(productService.getGuideById(1)).thenReturn(
                new GuideDTO("금속류는 이물질을 제거하고 배출하세요.", "https://example.com/images/metal.png")
        );

        mockMvc.perform(get("/api/guides/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guideText").value("금속류는 이물질을 제거하고 배출하세요."))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/images/metal.png"));
    }
}