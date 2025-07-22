package infinite_loop.sejonghack.service;

import infinite_loop.sejonghack.domain.User;
import infinite_loop.sejonghack.dto.*;
import infinite_loop.sejonghack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    /**
     * ✅ 테스트용: 마이페이지 - 사용자 정보 조회 (UserMyPageResponseDto)
     */
    public UserMyPageResponseDto getMyPage(Long userId) {
        // 테스트용 임시 데이터 반환 (실제 DB 연동 시 수정 필요)
        return new UserMyPageResponseDto(
                "홍길동",
                "hong@test.com",
                "길동이",
                "https://example.com/profile.jpg",
                1230
        );
    }

    /**
     * ✅ 테스트용: 최근 분리배출 검색어 조회
     */
    public List<RecentSearchResponseDto> getRecentSearches(Long userId) {
        return List.of(
                new RecentSearchResponseDto("플라스틱컵", "plastic.jpg"),
                new RecentSearchResponseDto("캔", "can.jpg")
        );
    }
}
