package infinite_loop.sejonghack.controller;

import infinite_loop.sejonghack.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Test
    @DisplayName("현재 보유 포인트 조회 성공")
    void getMyPointSuccess() throws Exception {
        when(pointService.getCurrentPoint(1L)).thenReturn(3200);

        mockMvc.perform(get("/api/points/me")
                        .with(user("testUser").roles("USER"))) // ✅ 인증 추가
                .andExpect(status().isOk())
                .andExpect(content().string("3200"));
    }

    @Test
    @DisplayName("포인트 적립/사용 내역 조회 성공")
    void getPointHistorySuccess() throws Exception {
        // 이 부분은 적절한 DTO 응답 mocking 필요
        mockMvc.perform(get("/api/points/history")
                        .with(user("testUser").roles("USER"))) // ✅ 인증 추가
                .andExpect(status().isOk());
    }
}
