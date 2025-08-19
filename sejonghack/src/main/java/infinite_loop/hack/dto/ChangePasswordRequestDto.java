package infinite_loop.hack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChangePasswordRequestDto {
    private String currentPassword;
    private String newPassword;
}


