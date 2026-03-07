package net.causw.app.main.domain.user.account.api.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSignOutRequestDto {
	private String refreshToken;
	private String accessToken;
	private String fcmToken;
}
