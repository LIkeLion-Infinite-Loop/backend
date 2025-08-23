// src/main/java/infinite_loop/hack/receipt/dto/DisposalListResponseDto.java
package infinite_loop.hack.receipt.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class DisposalListResponseDto {
    private int total;
    private List<DisposalRecordDto> items;
}