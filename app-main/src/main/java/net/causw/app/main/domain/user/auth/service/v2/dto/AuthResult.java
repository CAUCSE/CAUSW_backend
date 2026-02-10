package net.causw.app.main.domain.user.auth.service.v2.dto;

import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;

public record AuthResult(
	AuthResponse authResponse,
	String refreshToken) {
	public static AuthResult of(AuthResponse authResponse, String refreshToken) {
		return new AuthResult(authResponse, refreshToken);
	}
}
