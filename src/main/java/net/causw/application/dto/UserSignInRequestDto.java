package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignInRequestDto {
    private String email;
    private String password;
}
