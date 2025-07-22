package infinite_loop.sejonghack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import infinite_loop.sejonghack.dto.InquiryRequestDto;
import infinite_loop.sejonghack.service.InquiryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;



import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InquiryController.class)
public class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InquiryService inquiryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("문의 등록 성공")
    void testSubmitInquiry() throws Exception {
        InquiryRequestDto dto = new InquiryRequestDto();
        dto.setTitle("문의 제목입니다");
        dto.setContent("문의 내용입니다");

        // 서비스 로직은 아무 동작도 하지 않음 (void 메서드)
        doNothing().when(inquiryService).submitInquiry(1L, dto);

        mockMvc.perform(post("/api/support/contact")
                        .with(csrf())
                        .with(user("testUser").roles("USER"))  // ✅ 인증된 유저로 요청
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("문의가 등록되었습니다."));

    }
}
