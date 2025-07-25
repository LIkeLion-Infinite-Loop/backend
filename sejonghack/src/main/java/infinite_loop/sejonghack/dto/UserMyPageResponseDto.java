package infinite_loop.sejonghack.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserMyPageResponseDto {
    private String name;
    private String email;
    private String nickname;
    private String profile_img;
    private int total_point;
}
