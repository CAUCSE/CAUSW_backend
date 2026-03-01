package net.causw.app.main.domain.user.auth.service.dto;

import net.causw.app.main.domain.user.account.enums.user.UserState;

public record AuthResult(
	String accessToken,
	String name,
	String email,
	String profileImgUrl,
	String refreshToken,
	UserState userState) {
	public static AuthResult of(String accessToken,
		String name,
		String email,
		String profileImgUrl,
		String refreshToken,
		UserState userState) {
		return new AuthResult(accessToken, name, email, profileImgUrl, refreshToken, userState);
	}
}
