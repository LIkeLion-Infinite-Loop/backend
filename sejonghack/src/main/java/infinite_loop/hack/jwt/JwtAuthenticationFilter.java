package infinite_loop.hack.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import infinite_loop.hack.security.CustomUserDetailsService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        System.out.println("[JwtFilter] 토큰: " + token);

        if (token != null && jwtProvider.validateToken(token)) {
            try {
                Claims claims = jwtProvider.parseToken(token);
                String email = claims.getSubject();
                System.out.println("[JwtFilter] 이메일 from 토큰: " + email);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println("[JwtFilter] userDetails: " + userDetails);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("[JwtFilter] 인증 성공: " + userDetails.getUsername());

            } catch (Exception e) {
                System.out.println("[JwtFilter] 인증 실패: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[JwtFilter] 토큰 없음 또는 유효하지 않음");
        }


        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}