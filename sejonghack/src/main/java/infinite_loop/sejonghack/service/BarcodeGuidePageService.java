package infinite_loop.sejonghack.service;

import infinite_loop.sejonghack.domain.BarcodeGuidePage;
import infinite_loop.sejonghack.repository.BarcodeGuidePageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BarcodeGuidePageService {

    private final BarcodeGuidePageRepository barcodeGuidePageRepository;

    // 가이드 ID로 가이드 조회
    public BarcodeGuidePage findById(Long guideId) {
        return barcodeGuidePageRepository.findById(guideId)
                .orElseThrow(() -> new NoSuchElementException("가이드를 찾을 수 없습니다."));
    }

}