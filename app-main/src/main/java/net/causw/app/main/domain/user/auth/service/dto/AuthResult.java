package net.causw.app.main.domain.user.auth.service.dto;

import net.causw.app.main.shared.dto.ProfileImageDto;

public record AuthResult(
	String accessToken,
	String name,
	String email,
	ProfileImageDto profileImage,
	String refreshToken) {
	public static AuthResult of(String accessToken,
		String name,
		String email,
		ProfileImageDto profileImage,
		String refreshToken) {
		return new AuthResult(accessToken, name, email, profileImage, refreshToken);
	}
}
