package net.causw.application.dto.user;

import lombok.Getter;

@Getter
public class UserSignOutRequestDto {
    private String refreshToken;
    private String accessToken;
}
