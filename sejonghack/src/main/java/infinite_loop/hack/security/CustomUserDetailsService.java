package infinite_loop.hack.security;

import infinite_loop.hack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("[CustomUserDetailsService] 사용자 이메일 요청됨: " + email);

        return userRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> {
                    System.out.println("[CustomUserDetailsService] 사용자 없음: " + email);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
                });
    }
}