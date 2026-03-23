package net.causw.app.main.domain.user.auth.handler;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import net.causw.app.main.domain.user.auth.util.OAuthRedirectResolver;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private final OAuthRedirectResolver oAuthRedirectResolver;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {

		String errorMessage = "알 수 없는 오류가 발생했습니다.";
		String errorCode = "UNKNOWN_ERROR";
		int status = HttpServletResponse.SC_BAD_REQUEST;

		Throwable cause = exception.getCause();
		String exceptionMessage = exception.getMessage();

		if (cause instanceof BaseRunTimeV2Exception baseException) {
			errorCode = baseException.getErrorCode().getCode();
			errorMessage = baseException.getMessage();
			status = baseException.getErrorCode().getStatus().value();
		} else if (exceptionMessage != null && exceptionMessage.contains("access_denied")) {
			errorCode = "USER_CANCELLED";
			errorMessage = "로그인을 취소하셨습니다.";
		} else if (exceptionMessage != null && exceptionMessage.contains("Email not found")) {
			errorCode = "MISSING_EMAIL";
			errorMessage = "이메일 정보가 필요합니다.";
		} else if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
			errorCode = oauth2Exception.getError().getErrorCode();
			errorMessage = "로그인을 취소하셨거나 권한을 제공하지 않으셨습니다.";
			status = HttpServletResponse.SC_UNAUTHORIZED;
		}

		String baseUrl = oAuthRedirectResolver.resolveRedirectBase(request);
		ResponseCookie envCookie = oAuthRedirectResolver.clearEnvCookie(request);
		response.addHeader(HttpHeaders.SET_COOKIE, envCookie.toString());

		String targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
			.queryParam("error", errorCode)
			.queryParam("status", status)
			.queryParam("message", errorMessage)
			.build().encode().toUriString();

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
