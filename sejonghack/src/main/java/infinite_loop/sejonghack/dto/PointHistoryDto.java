package infinite_loop.sejonghack.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PointHistoryDto {
    private int amount;
    private String reason;
    private LocalDateTime changedAt;
}
