package net.causw.app.main.domain.user.auth.service.dto;

public record AuthResult(
	String accessToken,
	String name,
	String email,
	String profileImgUrl,
	String refreshToken) {
	public static AuthResult of(String accessToken,
		String name,
		String email,
		String profileImgUrl,
		String refreshToken) {
		return new AuthResult(accessToken, name, email, profileImgUrl, refreshToken);
	}
}
