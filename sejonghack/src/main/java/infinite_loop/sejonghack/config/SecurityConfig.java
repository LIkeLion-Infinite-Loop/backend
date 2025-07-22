package infinite_loop.sejonghack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // ✅ 비밀번호 암호화에 사용할 인코더 Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ 테스트 및 간단 인증 제어용 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 테스트 및 개발 중 CSRF 비활성화 (실제 운영 시에는 켜야 함)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/support/contact",     // 문의하기
                                "/api/users/me",            // 마이페이지 조회
                                "/api/users/profile",       // 프로필 수정
                                "/api/users/password",      // 비밀번호 변경
                                "/api/users/recent-searches"// 최근 검색 목록
                        ).permitAll()
                        .anyRequest().authenticated()  // 그 외 경로는 인증 필요
                )
                .httpBasic(); // 기본 인증 (JWT 미사용 시 임시 사용)

        return http.build();
    }
}
