package net.causw.app.main.domain.user.auth.service.v2.dto;

public record AuthTokenPair(
	String accessToken,
	String refreshToken) {
}
