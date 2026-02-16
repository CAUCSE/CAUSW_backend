package net.causw.app.main.domain.user.auth.handler;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.auth.service.v2.implementation.AuthTokenManager;
import net.causw.global.constant.StaticValue;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Value("${app.auth.redirect-uri}")
	private String baseUrl;
	private final AuthTokenManager authTokenManager;
	private final UserReader userReader;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {

		// 1. Principal에서 유저 정보 추출
		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		String email = (String)oAuth2User.getAttributes().get("email");

		// 2. DB에서 실제 유저 엔티티 조회
		User user = userReader.findByEmailOrElseThrow(email);

		// 3. 리프레시토큰 생성 및 쿠키 저장
		String refreshToken = authTokenManager.createRefreshToken(user.getId());
		ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofMillis(StaticValue.JWT_REFRESH_TOKEN_VALID_TIME))
			.sameSite("Lax")
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		// 4. 상태에 따른 리다이렉트 경로 결정
		String targetUrl = determineTargetUrl(user.getState());

		// 5. 리다이렉트 실행
		if (response.isCommitted()) {
			return;
		}
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	private String determineTargetUrl(UserState state) {
		// GUEST 상태라면 추가 정보 입력 페이지로, 아니면 메인으로
		if (state == UserState.GUEST) {
			return UriComponentsBuilder.fromUriString(baseUrl)
				.queryParam("isFirstLogin", true)
				.build().toUriString();
		}
		return UriComponentsBuilder.fromUriString(baseUrl)
			.queryParam("isFirstLogin", false)
			.build().toUriString();
	}
}
