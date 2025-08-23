// src/main/java/infinite_loop/hack/receipt/controller/DisposalController.java
package infinite_loop.hack.receipt.controller;

import infinite_loop.hack.receipt.domain.DisposalHistory;
import infinite_loop.hack.receipt.dto.DisposalListResponseDto;
import infinite_loop.hack.receipt.dto.DisposalRecordDto;
import infinite_loop.hack.receipt.repository.DisposalHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disposals")
@RequiredArgsConstructor
public class DisposalController {

    private final DisposalHistoryRepository disposalHistoryRepository;

    /** 내가 확정한 내역 조회 */
    @GetMapping
    public ResponseEntity<DisposalListResponseDto> getMyDisposals(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        List<DisposalHistory> list = disposalHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        var items = list.stream()
                .map(d -> DisposalRecordDto.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .quantity(d.getQuantity())
                        .category(d.getCategory())
                        .disposed_at(d.getCreatedAt()) 
                        .build()
                )
                .toList();

        return ResponseEntity.ok(
                DisposalListResponseDto.builder()
                        .total(items.size())
                        .items(items)
                        .build()
        );
    }
}