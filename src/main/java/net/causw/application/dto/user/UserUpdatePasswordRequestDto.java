package net.causw.application.dto.user;

import lombok.Data;

@Data
public class UserUpdatePasswordRequestDto {
    private String originPassword;
    private String updatedPassword;
}
