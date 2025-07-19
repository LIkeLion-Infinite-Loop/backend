package infinite_loop.sejonghack.controller;

import infinite_loop.sejonghack.dto.BarcodeGuideResponseDto;
import infinite_loop.sejonghack.service.BarcodeGuideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guide/barcode")
public class BarcodeGuideController {

    private final BarcodeGuideService barcodeGuideService;

    public BarcodeGuideController(BarcodeGuideService barcodeGuideService) {
        this.barcodeGuideService = barcodeGuideService;
    }

    @GetMapping("/{barcode}")
    public ResponseEntity<BarcodeGuideResponseDto> getGuideByBarcode(@PathVariable String barcode) {
        BarcodeGuideResponseDto guide = barcodeGuideService.getGuideByBarcode(barcode);
        return ResponseEntity.ok(guide);
    }
}