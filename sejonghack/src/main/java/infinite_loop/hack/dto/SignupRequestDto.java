package infinite_loop.hack.dto;

import lombok.Getter;

@Getter
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
    private String nickname;
}