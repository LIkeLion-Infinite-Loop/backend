package infinite_loop.sejonghack.service;

import infinite_loop.sejonghack.domain.User;
import infinite_loop.sejonghack.dto.*;
import infinite_loop.sejonghack.jwt.JwtProvider;
import infinite_loop.sejonghack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;


    /**
     * 마이페이지 - 내 정보 조회 (UserResponseDto 버전)
     */
    public UserResponseDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        return UserResponseDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
                .totalPoint(user.getTotalPoint())
                .build();
    }

    /**
     * 마이페이지 - 프로필 수정
     */
    public void updateProfile(Long userId, UserUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        user.setNickname(dto.getNickname());
        user.setProfileImg(dto.getProfileImg());

        userRepository.save(user);
    }

    /**
     * 마이페이지 - 비밀번호 변경
     */
    public void changePassword(Long userId, PasswordChangeDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public void signup(SignupRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .totalPoint(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getEmail());

        return new LoginResponseDto(accessToken, refreshToken);
    }
}
