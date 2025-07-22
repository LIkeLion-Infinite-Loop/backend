package infinite_loop.sejonghack.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeDto {
    private String currentPassword;
    private String newPassword;
}
