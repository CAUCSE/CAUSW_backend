package net.causw.app.main.domain.moving.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignInResponseDto {
	private String accessToken;
	private String refreshToken;
}
