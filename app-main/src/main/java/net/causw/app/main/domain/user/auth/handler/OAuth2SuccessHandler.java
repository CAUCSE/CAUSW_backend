package net.causw.app.main.domain.user.auth.handler;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.auth.service.dto.CustomOAuth2User;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.util.OAuthRedirectResolver;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.global.constant.StaticValue;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * OAuth2/OIDC 소셜 로그인 성공 후 후처리를 담당하는 핸들러입니다.
 * <p>
 * 인증 principal을 기준으로 사용자 엔티티를 조회하고,
 * 리프레시 토큰 쿠키를 발급한 뒤 프론트 redirect URI로 이동시킵니다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final AuthTokenManager authTokenManager;
	private final UserReader userReader;
	private final OAuthRedirectResolver oAuthRedirectResolver;

	/**
	 * 소셜 로그인 성공 시 호출됩니다.
	 * <p>
	 * 1) principal로 사용자 조회, 2) 리프레시 토큰 쿠키 설정,
	 * 3) 사용자 상태 기반 redirect URL 생성 순서로 처리합니다.
	 *
	 * @param request 현재 HTTP 요청
	 * @param response 현재 HTTP 응답
	 * @param authentication 인증 컨텍스트
	 * @throws IOException redirect 처리 중 I/O 예외가 발생한 경우
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		User user = resolveAuthenticatedUser(authentication);

		// 리프레시토큰 생성 및 쿠키 저장
		String refreshToken = authTokenManager.createRefreshToken(user.getId());
		ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
			.httpOnly(false)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofMillis(StaticValue.JWT_REFRESH_TOKEN_VALID_TIME))
			.sameSite("None")
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		String baseUrl = oAuthRedirectResolver.resolveRedirectBase(request);
		ResponseCookie envCookie = oAuthRedirectResolver.clearEnvCookie(request);
		response.addHeader(HttpHeaders.SET_COOKIE, envCookie.toString());

		// 상태에 따른 리다이렉트 경로 결정
		String targetUrl = determineTargetUrl(user.getState(), baseUrl);

		// 리다이렉트 실행
		if (response.isCommitted()) {
			return;
		}
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	/**
	 * 인증 principal 타입에 맞춰 도메인 사용자 엔티티를 조회합니다.
	 * <p>
	 * Apple OIDC의 경우 email 우선 조회 후, email 누락 시 socialId(sub)로 fallback 조회합니다.
	 *
	 * @param authentication 인증 컨텍스트
	 * @return 로그인 대상 사용자 엔티티
	 */
	private User resolveAuthenticatedUser(Authentication authentication) {
		Object principal = authentication.getPrincipal();

		if (principal instanceof CustomOAuth2User customOAuth2User) {
			return userReader.findByEmailOrElseThrow(customOAuth2User.getEmail());
		}

		if (principal instanceof OidcUser oidcUser) {
			Optional<User> userByEmail = findByEmail(oidcUser.getEmail());
			return userByEmail.orElseGet(
				() -> userReader.findBySocialTypeAndSocialId(SocialType.APPLE, oidcUser.getSubject())
					.orElseThrow(AuthErrorCode.INVALID_TOKEN::toBaseException));
		}

		if (principal instanceof OAuth2User oAuth2User) {
			return findByEmail(getString(oAuth2User.getAttributes().get("email")))
				.orElseThrow(AuthErrorCode.INVALID_TOKEN::toBaseException);
		}

		throw AuthErrorCode.INVALID_TOKEN.toBaseException();
	}

	private Optional<User> findByEmail(String email) {
		if (!hasText(email)) {
			return Optional.empty();
		}
		return userReader.findByEmail(email);
	}

	private String getString(Object value) {
		if (value == null) {
			return null;
		}

		String text = String.valueOf(value);
		return hasText(text) ? text : null;
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	/**
	 * 사용자 상태에 따라 프론트 redirect URL을 생성합니다.
	 *
	 * @param state 사용자 상태
	 * @return redirect 대상 URL
	 */
	private String determineTargetUrl(UserState state, String baseUrl) {
		return UriComponentsBuilder.fromUriString(baseUrl)
			.build().toUriString();
	}
}
