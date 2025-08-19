package infinite_loop.hack.receipt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ConfirmRequest {
    private List<Long> selected_item_ids;
}