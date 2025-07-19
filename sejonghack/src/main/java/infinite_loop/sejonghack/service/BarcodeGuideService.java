package infinite_loop.sejonghack.service;

import infinite_loop.sejonghack.domain.BarcodeGuide;
import infinite_loop.sejonghack.dto.BarcodeGuideResponseDto;
import infinite_loop.sejonghack.repository.BarcodeGuideRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class BarcodeGuideService {

    private final BarcodeExternalApiService externalApiService;
    private final BarcodeGuideRepository repository;

    public BarcodeGuideService(BarcodeExternalApiService externalApiService, BarcodeGuideRepository repository) {
        this.externalApiService = externalApiService;
        this.repository = repository;
    }

    public BarcodeGuideResponseDto getGuideByBarcode(String barcode) {
        String productName = externalApiService.getProductNameByBarcode(barcode);

        BarcodeGuide guide = repository.findByProductNameContaining(productName)
                .orElseThrow(() -> new NoSuchElementException("등록된 분리수거 가이드가 없습니다."));

        return new BarcodeGuideResponseDto(guide);
    }
}