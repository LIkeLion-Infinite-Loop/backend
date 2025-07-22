package infinite_loop.sejonghack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import infinite_loop.sejonghack.config.TestSecurityConfig;
import infinite_loop.sejonghack.domain.User;
import infinite_loop.sejonghack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 기존 테스트 유저가 있다면 삭제하고 다시 등록
        userRepository.deleteAll();

        User user = User.builder()
                .email("test-user@test.com")
                .password("1234")
                .name("테스트유저")
                .nickname("test-nick")
                .profileImg("test-profile.png")
                .totalPoint(300)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    @Test
    @DisplayName("마이페이지 내 정보 조회")
    @WithUserDetails(
            value = "test-user@test.com",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void testGetMyPage() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("최근 검색어 조회")
    @WithUserDetails(
            value = "test-user@test.com",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void testRecentSearchList() throws Exception {
        mockMvc.perform(get("/api/users/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
