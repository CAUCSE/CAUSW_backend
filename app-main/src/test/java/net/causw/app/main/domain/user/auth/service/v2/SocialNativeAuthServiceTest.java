package net.causw.app.main.domain.user.auth.service.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.auth.service.CustomOAuth2UserService;
import net.causw.app.main.domain.user.auth.service.SocialNativeAuthService;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.dto.CustomOAuth2User;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

@ExtendWith(MockitoExtension.class)
class SocialNativeAuthServiceTest {

	private static final String PROVIDER_ACCESS_TOKEN = "provider-access-token";
	private static final String BEARER_PROVIDER_ACCESS_TOKEN = "Bearer provider-access-token";
	private static final String APP_ACCESS_TOKEN = "app-access-token";
	private static final String APP_REFRESH_TOKEN = "app-refresh-token";

	@InjectMocks
	private SocialNativeAuthService socialNativeAuthService;

	@Mock
	private ClientRegistrationRepository clientRegistrationRepository;

	@Mock
	private CustomOAuth2UserService customOAuth2UserService;

	@Mock
	private AuthTokenManager authTokenManager;

	@Test
	@DisplayName("성공: Kakao access token 검증 후 앱 토큰을 발급한다")
	void login_kakao_success() {
		ClientRegistration kakao = clientRegistration("kakao", "profile_nickname", "account_email");
		User user = mockUser();

		given(clientRegistrationRepository.findByRegistrationId("kakao")).willReturn(kakao);
		given(customOAuth2UserService.loadUser(any(OAuth2UserRequest.class)))
			.willReturn(new CustomOAuth2User(user, Map.of("id", "kakao-id"), "id"));
		given(authTokenManager.issueTokens(user, null))
			.willReturn(AuthTokenPair.of(APP_ACCESS_TOKEN, APP_REFRESH_TOKEN));

		AuthResult result = socialNativeAuthService.login("kakao", PROVIDER_ACCESS_TOKEN);

		assertThat(result.accessToken()).isEqualTo(APP_ACCESS_TOKEN);
		assertThat(result.refreshToken()).isEqualTo(APP_REFRESH_TOKEN);
		assertThat(result.email()).isEqualTo("user@cau.ac.kr");

		verify(customOAuth2UserService).loadUser(any(OAuth2UserRequest.class));
		verify(authTokenManager).issueTokens(user, null);
	}

	@Test
	@DisplayName("성공: Bearer 접두어가 포함된 access token도 정규화 후 검증한다")
	void login_kakao_success_with_bearer_prefix() {
		ClientRegistration kakao = clientRegistration("kakao", "profile_nickname", "account_email");
		User user = mockUser();

		given(clientRegistrationRepository.findByRegistrationId("kakao")).willReturn(kakao);
		given(customOAuth2UserService.loadUser(any(OAuth2UserRequest.class)))
			.willReturn(new CustomOAuth2User(user, Map.of("id", "kakao-id"), "id"));
		given(authTokenManager.issueTokens(user, null))
			.willReturn(AuthTokenPair.of(APP_ACCESS_TOKEN, APP_REFRESH_TOKEN));

		AuthResult result = socialNativeAuthService.login("kakao", BEARER_PROVIDER_ACCESS_TOKEN);

		assertThat(result.accessToken()).isEqualTo(APP_ACCESS_TOKEN);
		assertThat(result.refreshToken()).isEqualTo(APP_REFRESH_TOKEN);
	}

	@Test
	@DisplayName("실패: OIDC provider는 access-token-only 네이티브 플로우를 허용하지 않는다")
	void login_oidc_provider_not_supported() {
		ClientRegistration apple = clientRegistration("apple", "openid", "email", "name");

		given(clientRegistrationRepository.findByRegistrationId("apple")).willReturn(apple);

		assertThatThrownBy(() -> socialNativeAuthService.login("apple", PROVIDER_ACCESS_TOKEN))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.hasMessage(AuthErrorCode.INVALID_TOKEN.getMessage());

		verify(customOAuth2UserService, never()).loadUser(any(OAuth2UserRequest.class));
		verify(authTokenManager, never()).issueTokens(any(User.class), any());
	}

	@Test
	@DisplayName("실패: 소셜 로그인 도중 domain 예외가 발생하면 원인 예외를 그대로 전달한다")
	void login_rethrow_domain_exception() {
		ClientRegistration kakao = clientRegistration("kakao", "profile_nickname", "account_email");
		BaseRunTimeV2Exception domainException = AuthErrorCode.SOCIAL_EMAIL_REQUIRED.toBaseException();

		given(clientRegistrationRepository.findByRegistrationId("kakao")).willReturn(kakao);
		given(customOAuth2UserService.loadUser(any(OAuth2UserRequest.class)))
			.willThrow(new InternalAuthenticationServiceException("social login failed", domainException));

		assertThatThrownBy(() -> socialNativeAuthService.login("kakao", PROVIDER_ACCESS_TOKEN))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.hasMessage(AuthErrorCode.SOCIAL_EMAIL_REQUIRED.getMessage());

		verify(authTokenManager, never()).issueTokens(any(User.class), any());
	}

	@Test
	@DisplayName("실패: provider access token 검증 중 런타임 예외는 INVALID_TOKEN으로 매핑한다")
	void login_fail_when_provider_token_invalid() {
		ClientRegistration kakao = clientRegistration("kakao", "profile_nickname", "account_email");

		given(clientRegistrationRepository.findByRegistrationId("kakao")).willReturn(kakao);
		given(customOAuth2UserService.loadUser(any(OAuth2UserRequest.class)))
			.willThrow(new RuntimeException("provider rejected token"));

		assertThatThrownBy(() -> socialNativeAuthService.login("kakao", PROVIDER_ACCESS_TOKEN))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.hasMessage(AuthErrorCode.INVALID_TOKEN.getMessage());

		verify(authTokenManager, never()).issueTokens(any(User.class), any());
	}

	@Test
	@DisplayName("실패: 지원하지 않는 provider면 예외를 반환한다")
	void login_fail_when_provider_unsupported() {
		assertThatThrownBy(() -> socialNativeAuthService.login("naver", PROVIDER_ACCESS_TOKEN))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.hasMessage(AuthErrorCode.UNSUPPORTED_SOCIAL_PROVIDER.getMessage());
	}

	private ClientRegistration clientRegistration(String registrationId, String... scopes) {
		return ClientRegistration.withRegistrationId(registrationId)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.clientId(registrationId + "-client-id")
			.clientSecret(registrationId + "-client-secret")
			.authorizationUri("https://example.com/oauth/authorize")
			.tokenUri("https://example.com/oauth/token")
			.userInfoUri("https://example.com/oauth/userinfo")
			.userNameAttributeName("sub")
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
			.scope(scopes)
			.clientName(registrationId)
			.build();
	}

	private User mockUser() {
		User user = org.mockito.Mockito.mock(User.class);
		given(user.getId()).willReturn("user-id");
		given(user.getName()).willReturn("테스트유저");
		given(user.getEmail()).willReturn("user@cau.ac.kr");
		given(user.getProfileUrl()).willReturn("https://cdn.causw.net/profile/default.png");
		return user;
	}
}
