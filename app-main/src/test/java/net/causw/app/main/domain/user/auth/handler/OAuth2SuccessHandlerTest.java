package net.causw.app.main.domain.user.auth.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.auth.service.dto.CustomOAuth2User;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.implementation.SocialAccountOauthRefreshStore;
import net.causw.app.main.domain.user.auth.util.OAuthRedirectResolver;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

	private static final String REDIRECT_BASE = "https://front.test/callback";

	@Mock
	private AuthTokenManager authTokenManager;

	@Mock
	private UserReader userReader;

	@Mock
	private OAuthRedirectResolver oAuthRedirectResolver;

	@Mock
	private SocialAccountOauthRefreshStore socialAccountOauthRefreshStore;

	@InjectMocks
	private OAuth2SuccessHandler oAuth2SuccessHandler;

	@Test
	@DisplayName("OIDC 로그인은 email보다 socialId(sub)로 먼저 유저를 조회한다")
	void handleLoginSuccess_resolvesOidcUserBySocialIdFirst() throws Exception {
		//given: email claim은 다른 유저를 가리키지만, socialId로 확정된 유저는 userB
		User resolvedUser = mock(User.class);
		given(resolvedUser.getId()).willReturn("user-b");

		OidcIdToken idToken = new OidcIdToken(
			"apple-id-token",
			Instant.now(),
			Instant.MAX,
			Map.of(
				"sub", "apple-social-id",
				"email", "shared@test.com"));
		OidcUser principal = new DefaultOidcUser(Collections.singleton(() -> "ROLE_USER"), idToken, "sub");

		Authentication authentication = new OAuth2AuthenticationToken(
			principal, principal.getAuthorities(), "apple");

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		given(userReader.findBySocialTypeAndSocialId(SocialType.APPLE, "apple-social-id"))
			.willReturn(Optional.of(resolvedUser));
		given(oAuthRedirectResolver.resolveRedirectBase(request)).willReturn(REDIRECT_BASE);
		given(oAuthRedirectResolver.clearEnvCookie(request))
			.willReturn(ResponseCookie.from("oauth_env", "").maxAge(0).build());
		given(authTokenManager.createRefreshToken("user-b")).willReturn("issued-refresh-token");

		//when
		oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

		//then
		verify(authTokenManager).createRefreshToken("user-b");
		verify(userReader, never()).findByEmail(any());
		verify(userReader, never()).findByEmailOrElseThrow(any());
		assertTrue(response.getRedirectedUrl().contains("refreshToken=issued-refresh-token"));
	}

	@Test
	@DisplayName("OIDC 로그인에서 socialId로 못 찾으면 email로 fallback 조회한다")
	void handleLoginSuccess_fallsBackToEmailForOidcUser() throws Exception {
		User userByEmail = mock(User.class);
		given(userByEmail.getId()).willReturn("user-a");

		OidcIdToken idToken = new OidcIdToken(
			"google-id-token",
			Instant.now(),
			Instant.MAX,
			Map.of(
				"sub", "google-social-id",
				"email", "plain@test.com"));
		OidcUser principal = new DefaultOidcUser(Collections.singleton(() -> "ROLE_USER"), idToken, "sub");

		Authentication authentication = new OAuth2AuthenticationToken(
			principal, principal.getAuthorities(), "google");

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		given(userReader.findBySocialTypeAndSocialId(SocialType.GOOGLE, "google-social-id"))
			.willReturn(Optional.empty());
		given(userReader.findByEmail("plain@test.com")).willReturn(Optional.of(userByEmail));
		given(oAuthRedirectResolver.resolveRedirectBase(request)).willReturn(REDIRECT_BASE);
		given(oAuthRedirectResolver.clearEnvCookie(request))
			.willReturn(ResponseCookie.from("oauth_env", "").maxAge(0).build());
		given(authTokenManager.createRefreshToken("user-a")).willReturn("issued-refresh-token");

		//when
		oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

		//then
		verify(userReader).findBySocialTypeAndSocialId(SocialType.GOOGLE, "google-social-id");
		verify(userReader).findByEmail("plain@test.com");
		verify(authTokenManager).createRefreshToken("user-a");
	}

	@Test
	@DisplayName("카카오(CustomOAuth2User) 로그인 경로는 기존 동작을 그대로 유지한다")
	void handleLoginSuccess_keepsExistingPathForCustomOAuth2User() throws Exception {
		User kakaoUser = mock(User.class);
		given(kakaoUser.getEmail()).willReturn("kakao@test.com");
		given(kakaoUser.getId()).willReturn("user-kakao");

		CustomOAuth2User principal = new CustomOAuth2User(
			kakaoUser,
			Map.of("id", "kakao-social-id"),
			"id");

		Authentication authentication = new OAuth2AuthenticationToken(
			principal, Collections.singleton(() -> "ROLE_USER"), "kakao");

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		given(userReader.findByEmailOrElseThrow("kakao@test.com")).willReturn(kakaoUser);
		given(oAuthRedirectResolver.resolveRedirectBase(request)).willReturn(REDIRECT_BASE);
		given(oAuthRedirectResolver.clearEnvCookie(request))
			.willReturn(ResponseCookie.from("oauth_env", "").maxAge(0).build());
		given(authTokenManager.createRefreshToken("user-kakao")).willReturn("issued-refresh-token");

		oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

		verify(userReader).findByEmailOrElseThrow("kakao@test.com");
		verify(authTokenManager).createRefreshToken("user-kakao");
	}
}
