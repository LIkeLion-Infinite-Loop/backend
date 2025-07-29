package infinite_loop.sejonghack.dto;

import lombok.Getter;

@Getter
public class ChangePasswordRequestDto {
    private String currentPassword;
    private String newPassword;
}

