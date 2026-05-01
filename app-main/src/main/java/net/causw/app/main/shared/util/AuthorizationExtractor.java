package net.causw.app.main.shared.util;

import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import jakarta.servlet.http.HttpServletRequest;

public class AuthorizationExtractor {

	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String REFRESH_AUTHORIZATION_HEADER = "Refresh-Authorization";
	public static final String BEARER_PREFIX = "Bearer ";

	public static void validate(String authHeader) {
		String token = normalizeToken(authHeader);
		if (token == null || token.isBlank()) {
			throw AuthErrorCode.NO_PERMISSION_FOR_RESOURCE.toBaseException();
		}
	}

	public static void validateRefresh(String refreshAuthHeader) {
		String token = normalizeToken(refreshAuthHeader);
		if (token == null || token.isBlank()) {
			throw AuthErrorCode.REFRESH_TOKEN_MISSING.toBaseException();
		}
	}

	public static String extract(String authHeader) {
		return normalizeToken(authHeader);
	}

	public static String extractRefresh(String refreshAuthHeader) {
		return extract(refreshAuthHeader);
	}

	public static String extract(HttpServletRequest request) {
		String header = request.getHeader(AUTHORIZATION_HEADER);
		return extract(header);
	}

	private static String normalizeToken(String headerValue) {
		if (headerValue == null) {
			return null;
		}

		String trimmed = headerValue.trim();
		if (trimmed.isBlank()) {
			return null;
		}

		if (trimmed.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
			String token = trimmed.substring(BEARER_PREFIX.length()).trim();
			return token.isBlank() ? null : token;
		}

		return trimmed;
	}
}
