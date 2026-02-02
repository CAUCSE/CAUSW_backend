package net.causw.app.main.domain.user.auth.service.v2.dto;

import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;

public record AuthInternalDto(
	AuthResponse authResponse,
	String refreshToken) {
	public static AuthInternalDto of(AuthResponse authResponse, String refreshToken) {
		return new AuthInternalDto(authResponse, refreshToken);
	}
}
