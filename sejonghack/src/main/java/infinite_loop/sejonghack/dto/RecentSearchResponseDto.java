package infinite_loop.sejonghack.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RecentSearchResponseDto {
    private String keyword;
    private String imagePath;
}
