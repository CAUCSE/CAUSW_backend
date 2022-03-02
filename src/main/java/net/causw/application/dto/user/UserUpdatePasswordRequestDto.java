package net.causw.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserUpdatePasswordRequestDto {
    private String originPassword;
    private String updatedPassword;
}
