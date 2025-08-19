package infinite_loop.hack.repository;

import infinite_loop.hack.domain.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    // 현재는 기본 save(), findById()만 사용 예정
}
