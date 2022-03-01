package net.causw.application.dto.user;

import lombok.Data;

@Data
public class UserSignInRequestDto {
    private String email;
    private String password;
}
