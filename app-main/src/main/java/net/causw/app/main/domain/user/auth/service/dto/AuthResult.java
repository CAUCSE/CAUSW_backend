package net.causw.app.main.domain.user.auth.service.dto;

import net.causw.app.main.domain.user.account.enums.user.ProfileImageType;

public record AuthResult(
	String accessToken,
	String name,
	String email,
	ProfileImageType profileImageType,
	String profileImgUrl,
	String refreshToken) {
	public static AuthResult of(String accessToken,
		String name,
		String email,
		ProfileImageType profileImageType,
		String profileImgUrl,
		String refreshToken) {
		return new AuthResult(accessToken, name, email, profileImageType, profileImgUrl, refreshToken);
	}
}
