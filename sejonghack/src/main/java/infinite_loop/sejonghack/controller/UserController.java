package infinite_loop.sejonghack.controller;

import infinite_loop.sejonghack.dto.*;
import infinite_loop.sejonghack.security.CustomUserDetails;
import infinite_loop.sejonghack.service.SearchHistoryService;
import infinite_loop.sejonghack.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SearchHistoryService searchHistoryService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestDto dto) {
        userService.signup(dto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(userService.login(dto));
    }

    // ✅ 마이페이지 - 사용자 정보
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyPage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(userService.getMyInfo(userId));
    }

    // ✅ 마이페이지 - 최근 검색 목록
    @GetMapping("/recent-searches")
    public ResponseEntity<List<String>> getRecentSearches(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(searchHistoryService.getRecentSearchKeywords(userId));
    }

    // ✅ 마이페이지 - 프로필 수정
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @RequestBody UserUpdateDto dto) {
        Long userId = userDetails.getUser().getId();
        userService.updateProfile(userId, dto);
        return ResponseEntity.ok("프로필 수정 완료");
    }

    // ✅ 마이페이지 - 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestBody PasswordChangeDto dto) {
        Long userId = userDetails.getUser().getId();
        userService.changePassword(userId, dto);
        return ResponseEntity.ok("비밀번호 변경 완료");
    }
}