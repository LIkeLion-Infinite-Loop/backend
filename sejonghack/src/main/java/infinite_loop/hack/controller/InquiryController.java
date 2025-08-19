package infinite_loop.hack.controller;

import infinite_loop.hack.dto.InquiryRequestDto;
import infinite_loop.hack.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping("/contact")
    public ResponseEntity<String> submitInquiry(@RequestBody InquiryRequestDto dto) {
        Long userId = 1L; // JWT 미적용 환경 기준
        inquiryService.submitInquiry(userId, dto);
        return ResponseEntity.ok("문의가 등록되었습니다.");
    }
}
