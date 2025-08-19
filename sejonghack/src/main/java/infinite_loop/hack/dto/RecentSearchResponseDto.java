package infinite_loop.hack.dto;

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
