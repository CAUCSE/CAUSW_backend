package net.causw.app.main.shared.util;

import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import jakarta.servlet.http.HttpServletRequest;

public class AuthorizationExtractor {

	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String BEARER_PREFIX = "Bearer ";

	public static void validate(String authHeader) {
		if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
			throw AuthErrorCode.NO_PERMISSION_FOR_RESOURCE.toBaseException();
		}
	}

	public static String extract(String authHeader) {
		if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
			return authHeader.substring(BEARER_PREFIX.length());
		}
		return null;
	}

	public static String extract(HttpServletRequest request) {
		String header = request.getHeader(AUTHORIZATION_HEADER);
		return extract(header);
	}
}
