package infinite_loop.sejonghack.service;

import infinite_loop.sejonghack.domain.Inquiry;
import infinite_loop.sejonghack.domain.User;
import infinite_loop.sejonghack.dto.InquiryRequestDto;
import infinite_loop.sejonghack.repository.InquiryRepository;
import infinite_loop.sejonghack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    public void submitInquiry(Long userId, InquiryRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();

        inquiryRepository.save(inquiry);
    }
}
