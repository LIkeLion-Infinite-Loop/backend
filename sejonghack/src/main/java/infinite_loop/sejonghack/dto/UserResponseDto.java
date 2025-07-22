package infinite_loop.sejonghack.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponseDto {
    private String name;
    private String email;
    private String nickname;
    private String profileImg;
    private Integer totalPoint;
}
