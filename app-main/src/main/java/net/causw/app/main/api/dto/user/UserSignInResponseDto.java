package net.causw.app.main.api.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignInResponseDto {
	private String accessToken;
	private String refreshToken;
}
