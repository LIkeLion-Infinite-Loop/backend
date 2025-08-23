// src/main/java/infinite_loop/hack/receipt/dto/DisposalRecordDto.java
package infinite_loop.hack.receipt.dto;

import infinite_loop.hack.receipt.domain.Category;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class DisposalRecordDto {
    private Long id;
    private String name;
    private Integer quantity;
    private Category category;
    private LocalDateTime disposed_at;
}