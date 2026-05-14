package net.causw.app.main.domain.user.auth.handler;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.auth.service.dto.CustomOAuth2User;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.implementation.OAuth2RefreshTokenCaptureClient;
import net.causw.app.main.domain.user.auth.service.implementation.OAuthLinkTokenStore;
import net.causw.app.main.domain.user.auth.service.implementation.SocialAccountOauthRefreshStore;
import net.causw.app.main.domain.user.auth.util.OAuthRedirectResolver;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * OAuth2/OIDC 소셜 로그인 성공 후 후처리를 담당하는 핸들러입니다.
 * <p>
 * request attribute {@link OAuthLinkTokenStore#LINK_USER_ID_ATTR} 유무를 기준으로
 * 소셜 계정 연동 플로우와 로그인 플로우를 분기합니다.
 * <ul>
 *   <li>연동 플로우: 계정 연동은 {@link net.causw.app.main.domain.user.auth.service.CustomOAuth2UserService}에서
 *       이미 완료됐으므로, 핸들러는 프론트로 redirect만 수행합니다.</li>
 *   <li>로그인 플로우: 인증 principal을 기준으로 사용자 엔티티를 조회하고,
 * 	     리프레시 토큰을 발급한 뒤 프론트 redirect URI의 쿼리 파라미터로 전달합니다.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final AuthTokenManager authTokenManager;
	private final UserReader userReader;
	private final OAuthRedirectResolver oAuthRedirectResolver;
	private final SocialAccountOauthRefreshStore socialAccountOauthRefreshStore;

	/**
	 * 소셜 로그인 성공 시 호출됩니다.
	 * <p>
	 * request attribute {@link OAuthLinkTokenStore#LINK_USER_ID_ATTR}가 존재하면 연동 플로우,
	 * 없으면 로그인 플로우로 분기합니다.
	 *
	 * @param request        현재 HTTP 요청
	 * @param response       현재 HTTP 응답
	 * @param authentication 인증 컨텍스트
	 * @throws IOException redirect 처리 중 I/O 예외가 발생한 경우
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		String linkUserId = (String)request.getAttribute(OAuthLinkTokenStore.LINK_USER_ID_ATTR);

		if (linkUserId != null) {
			handleLinkSuccess(request, response, authentication);
			return;
		}

		handleLoginSuccess(request, response, authentication);
	}

	// ── 로그인 플로우 ──────────────────────────────────────────────────────────

	/**
	 * 소셜 로그인 성공 처리입니다.
	 * <p>
	 * 리프레시 토큰을 쿼리 파라미터로 포함하여 프론트 redirect URI로 이동합니다.
	 */
	private void handleLoginSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		User user = resolveAuthenticatedUser(authentication);
		saveProviderOAuthRefreshTokenIfPresent(request, authentication, user);

		String refreshToken = authTokenManager.createRefreshToken(user.getId());

		String baseUrl = oAuthRedirectResolver.resolveRedirectBase(request);
		ResponseCookie envCookie = oAuthRedirectResolver.clearEnvCookie(request);
		response.addHeader(HttpHeaders.SET_COOKIE, envCookie.toString());

		// 상태에 따른 리다이렉트 경로 결정
		String targetUrl = determineTargetUrl(baseUrl, refreshToken);

		if (response.isCommitted()) {
			return;
		}
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	// ── 연동 플로우 ──────────────────────────────────────────────────────────

	/**
	 * 소셜 계정 연동 성공 처리입니다.
	 * <p>
	 * 계정 연동은 {@link net.causw.app.main.domain.user.auth.service.CustomOAuth2UserService}에서
	 * 이미 완료됐으므로, {@code linked={provider}} 쿼리 파라미터와 함께 프론트로 redirect합니다.
	 * 연동 실패 시에는 Spring Security가 {@link OAuth2FailureHandler}로 자동 위임합니다.
	 */
	private void handleLinkSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {

		String baseUrl = oAuthRedirectResolver.resolveRedirectBase(request);
		response.addHeader(HttpHeaders.SET_COOKIE, oAuthRedirectResolver.clearEnvCookie(request).toString());

		String registrationId = ((OAuth2AuthenticationToken)authentication).getAuthorizedClientRegistrationId();
		String targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
			.queryParam("linked", registrationId)
			.build().toUriString();

		if (response.isCommitted()) {
			return;
		}
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	// ── 헬퍼 메서드 ──────────────────────────────────────────────────────────

	private void saveProviderOAuthRefreshTokenIfPresent(HttpServletRequest request, Authentication authentication,
		User user) {
		if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
			return;
		}
		String regId = oauth2Token.getAuthorizedClientRegistrationId();
		if (!SocialType.GOOGLE.matchesRegistrationId(regId) && !SocialType.APPLE.matchesRegistrationId(regId)) {
			return;
		}
		String refresh = (String)request.getAttribute(OAuth2RefreshTokenCaptureClient.REQUEST_ATTR_REFRESH_TOKEN);
		if (!StringUtils.hasText(refresh)) {
			return;
		}
		SocialType type = SocialType.from(regId);
		socialAccountOauthRefreshStore.saveEncryptedRefreshToken(user.getId(), type, refresh);
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
	 * @param baseUrl 기본 URL
	 * @param refreshToken 리다이렉트 URL에 포함할 리프레시 토큰
	 * @return redirect 대상 URL
	 */
	private String determineTargetUrl(String baseUrl, String refreshToken) {
		return UriComponentsBuilder.fromUriString(baseUrl)
			.queryParam("refreshToken", refreshToken)
			.build().toUriString();
	}
}
