package net.causw.application.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignInResponseDto {
    private String accessToken;
    private String refreshToken;
}
