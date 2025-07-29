package infinite_loop.sejonghack.controller;

import infinite_loop.sejonghack.domain.User;
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

    //마이페이지 - 사용자 정보
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyPage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyInfo(userDetails.getId()));
    }

    //마이페이지 - 최근 검색 목록
    @GetMapping("/recent-searches")
    public ResponseEntity<List<String>> getRecentSearches(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(searchHistoryService.getRecentSearchKeywords(userId));
    }

    //마이페이지 - 프로필 수정
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @RequestBody UserUpdateDto dto) {
        Long userId = userDetails.getUser().getId();
        userService.updateProfile(userId, dto);
        return ResponseEntity.ok("프로필 수정 완료");
    }


    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        userService.sendTemporaryPassword(request.getEmail());
        return ResponseEntity.ok(new ResetPasswordResponseDto("임시 비밀번호가 이메일로 전송되었습니다"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordResponseDto> changePassword(
            @RequestBody ChangePasswordRequestDto request,
            @AuthenticationPrincipal User user) {
        userService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(new ChangePasswordResponseDto("비밀번호가 성공적으로 변경되었습니다"));
    }
}