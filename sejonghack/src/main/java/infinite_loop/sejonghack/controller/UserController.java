package infinite_loop.sejonghack.controller;

import infinite_loop.sejonghack.dto.UserResponseDto;
import infinite_loop.sejonghack.dto.UserUpdateDto;
import infinite_loop.sejonghack.service.SearchHistoryService;
import infinite_loop.sejonghack.service.UserService;
import infinite_loop.sejonghack.dto.PasswordChangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SearchHistoryService searchHistoryService;

    // ✅ 마이페이지 - 사용자 정보
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyPage() {
        Long userId = 1L; // 임시
        return ResponseEntity.ok(userService.getMyInfo(userId));
    }

    // ✅ 마이페이지 - 최근 검색 목록
    @GetMapping("/recent-searches")
    public ResponseEntity<List<String>> getRecentSearches() {
        Long userId = 1L; // 임시
        return ResponseEntity.ok(searchHistoryService.getRecentSearchKeywords(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@RequestBody UserUpdateDto dto) {
        Long userId = 1L; // JWT 적용 전 하드코딩
        userService.updateProfile(userId, dto);
        return ResponseEntity.ok("프로필 수정 완료");
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeDto dto) {
        Long userId = 1L; // 임시 (JWT 적용 전)
        userService.changePassword(userId, dto);
        return ResponseEntity.ok("비밀번호 변경 완료");
    }

}
