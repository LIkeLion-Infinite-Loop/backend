package infinite_loop.sejonghack.dto;

import lombok.Getter;

@Getter
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
    private String nickname;
}