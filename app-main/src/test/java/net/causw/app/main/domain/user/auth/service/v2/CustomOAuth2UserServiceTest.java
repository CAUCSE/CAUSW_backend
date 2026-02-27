package net.causw.app.main.domain.user.auth.service.v2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.CustomOAuth2UserService;
import net.causw.app.main.domain.user.auth.service.dto.CustomOAuth2User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

	@InjectMocks
	private CustomOAuth2UserService customOAuth2UserService;

	@Mock
	private UserReader userReader;

	@Mock
	private UserWriter userWriter;

	@Mock
	private UserValidator userValidator;

	private OAuth2UserRequest userRequest;
	private OAuth2User oAuth2User;
	private User testUser;

	@BeforeEach
	void setUp() {
		ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.clientId("test-client")
			.authorizationUri("https://auth.uri")
			.tokenUri("https://token.uri")
			.userInfoUri("https://userinfo.uri")
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
			.userNameAttributeName("sub")
			.build();

		OAuth2AccessToken accessToken = new OAuth2AccessToken(
			OAuth2AccessToken.TokenType.BEARER, "test-token", Instant.now(), Instant.MAX);

		userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

		Map<String, Object> attributes = Map.of(
			"sub", "test-social-id",
			"name", "Test User",
			"email", "test@test.com",
			"email_verified", true);

		oAuth2User = new DefaultOAuth2User(
			Collections.singleton(() -> "ROLE_USER"),
			attributes,
			"sub");

		testUser = mock(User.class);
	}

	@Test
	@DisplayName("소셜 계정이 이미 존재하는 경우 (기존 유저 로그인)")
	void loadUser_ExistingSocialAccount() {
		//given
		try (MockedConstruction<DefaultOAuth2UserService> mocked = mockConstruction(DefaultOAuth2UserService.class,
			(mock, context) -> when(mock.loadUser(any())).thenReturn(oAuth2User))) {
			when(userReader.findBySocialTypeAndSocialId(any(), any())).thenReturn(Optional.of(testUser));

			//when
			CustomOAuth2User result = customOAuth2UserService.loadUser(userRequest);

			//then
			assertNotNull(result);

			//verify
			verify(userReader).findBySocialTypeAndSocialId(any(), any());
			verify(userValidator).validateUserStatusForLogin(any());
			verify(userWriter, never()).save((User)any());
		}
	}

	@Test
	@DisplayName("소셜 계정은 없지만 같은 이메일 유저가 존재하는 경우 (계정 통합)")
	void loadUser_IntegrationExistingEmailUser() {
		//given
		try (MockedConstruction<DefaultOAuth2UserService> mocked = mockConstruction(DefaultOAuth2UserService.class,
			(mock, context) -> when(mock.loadUser(any())).thenReturn(oAuth2User))) {
			when(userReader.findBySocialTypeAndSocialId(any(), any())).thenReturn(Optional.empty());
			when(userReader.findByEmail(any())).thenReturn(Optional.of(testUser));

			//when
			CustomOAuth2User result = customOAuth2UserService.loadUser(userRequest);

			//then
			assertNotNull(result);

			//verify
			verify(userValidator).checkAccountExistByUserAndSocialType(any(), any());
			verify(userValidator).validateUserStatusForIntegration(any());
			verify(userWriter).save(any(SocialAccount.class));
			verify(userWriter, never()).save(any(User.class));
		}
	}

	@Test
	@DisplayName("신규 유저인 경우 (GUEST 가입)")
	void loadUser_NewUser() {
		//given
		try (MockedConstruction<DefaultOAuth2UserService> mocked = mockConstruction(DefaultOAuth2UserService.class,
			(mock, context) -> when(mock.loadUser(any())).thenReturn(oAuth2User))) {
			when(userReader.findBySocialTypeAndSocialId(any(), any())).thenReturn(Optional.empty());
			when(userReader.findByEmail(any())).thenReturn(Optional.empty());

			//when
			CustomOAuth2User result = customOAuth2UserService.loadUser(userRequest);

			//then
			assertNotNull(result);

			//verify
			verify(userWriter).save(any(User.class));
			verify(userWriter).save(any(SocialAccount.class));
		}
	}

	@Test
	@DisplayName("이메일 유저가 존재하지만 이메일 미인증 상태인 경우 예외 발생")
	void loadUser_UnverifiedEmail_ThrowsException() {
		//given
		Map<String, Object> unverifiedAttributes = Map.of(
			"sub", "test-social-id",
			"name", "Test User",
			"email", "test@test.com",
			"email_verified", false);
		OAuth2User unverifiedOAuth2User = new DefaultOAuth2User(
			Collections.singleton(() -> "ROLE_USER"),
			unverifiedAttributes,
			"sub");

		try (MockedConstruction<DefaultOAuth2UserService> mocked = mockConstruction(DefaultOAuth2UserService.class,
			(mock, context) -> when(mock.loadUser(any())).thenReturn(unverifiedOAuth2User))) {
			when(userReader.findBySocialTypeAndSocialId(any(), any())).thenReturn(Optional.empty());
			when(userReader.findByEmail(any())).thenReturn(Optional.of(testUser));

			//when
			//then
			assertThrows(InternalAuthenticationServiceException.class, () -> {
				customOAuth2UserService.loadUser(userRequest);
			});

			//verify
			verify(userValidator, never()).checkAccountExistByUserAndSocialType(any(), any());
			verify(userWriter, never()).save((User)any());
		}
	}

	@Test
	@DisplayName("로그인 불가능한 유저 상태일 경우 예외 발생 (validateUserStatusForLogin 실패)")
	void loadUser_InvalidUserStatusForLogin_ThrowsException() {
		//given
		try (MockedConstruction<DefaultOAuth2UserService> mocked = mockConstruction(DefaultOAuth2UserService.class,
			(mock, context) -> when(mock.loadUser(any())).thenReturn(oAuth2User))) {

			// 기존 유저가 존재한다고 가정
			when(userReader.findBySocialTypeAndSocialId(any(), any())).thenReturn(Optional.of(testUser));

			// Validator가 deletedAt이 설정된 탈퇴 유저로 판단하여 예외를 던지도록 Mocking
			doThrow(UserErrorCode.INVALID_LOGIN_USER_DELETED.toBaseException())
				.when(userValidator).validateUserStatusForLogin(any());

			//when & then
			assertThrows(InternalAuthenticationServiceException.class, () -> {
				customOAuth2UserService.loadUser(userRequest);
			});

			//verify
			verify(userValidator).validateUserStatusForLogin(any());
		}
	}

	@Test
	@DisplayName("계정 통합이 불가능한 유저 상태일 경우 예외 발생 (validateUserStatusForIntegration 실패)")
	void loadUser_InvalidUserStatusForIntegration_ThrowsException() {
		//given
		try (MockedConstruction<DefaultOAuth2UserService> mocked = mockConstruction(DefaultOAuth2UserService.class,
			(mock, context) -> when(mock.loadUser(any())).thenReturn(oAuth2User))) {

			when(userReader.findBySocialTypeAndSocialId(any(), any())).thenReturn(Optional.empty());
			when(userReader.findByEmail(any())).thenReturn(Optional.of(testUser));

			// Validator가 추방된 유저(DROP) 상태로 판단하여 통합 불가 예외를 던지도록 Mocking
			doThrow(UserErrorCode.USER_DROPPED.toBaseException())
				.when(userValidator).validateUserStatusForIntegration(any());

			//when & then
			assertThrows(InternalAuthenticationServiceException.class, () -> {
				customOAuth2UserService.loadUser(userRequest);
			});

			//verify
			verify(userValidator).validateUserStatusForIntegration(any());
			verify(userWriter, never()).save(any(SocialAccount.class));
		}
	}

	@Test
	@DisplayName("OIDC 애플 로그인 시 신규 유저를 생성한다")
	void loadOidcUser_AppleNewUser() {
		// given
		ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("apple")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.clientId("apple-client")
			.authorizationUri("https://appleid.apple.com/auth/authorize")
			.tokenUri("https://appleid.apple.com/auth/token")
			.jwkSetUri("https://appleid.apple.com/auth/keys")
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
			.scope("openid", "email", "name")
			.userInfoUri("https://appleid.apple.com/auth/userinfo")
			.userNameAttributeName("sub")
			.clientName("Apple")
			.build();

		OAuth2AccessToken accessToken = new OAuth2AccessToken(
			OAuth2AccessToken.TokenType.BEARER, "apple-access-token", Instant.now(), Instant.MAX);
		OidcIdToken idToken = new OidcIdToken(
			"apple-id-token",
			Instant.now(),
			Instant.MAX,
			Map.of(
				"sub", "apple-social-id",
				"email", "apple@test.com",
				"email_verified", true));

		OidcUserRequest oidcUserRequest = new OidcUserRequest(clientRegistration, accessToken, idToken);
		OidcUser oidcUser = new DefaultOidcUser(Collections.singleton(() -> "ROLE_USER"), idToken, "sub");

		try (MockedConstruction<OidcUserService> mocked = mockConstruction(OidcUserService.class,
			(mock, context) -> when(mock.loadUser(any())).thenReturn(oidcUser))) {
			when(userReader.findBySocialTypeAndSocialId(any(), any())).thenReturn(Optional.empty());
			when(userReader.findByEmail(any())).thenReturn(Optional.empty());

			// when
			OidcUser result = customOAuth2UserService.loadOidcUser(oidcUserRequest);

			// then
			assertNotNull(result);

			verify(userWriter).save(any(User.class));
			verify(userWriter).save(any(SocialAccount.class));
		}
	}

	@Test
	@DisplayName("OIDC 애플 로그인에서 email이 없어도 기존 소셜 계정이면 로그인된다")
	void loadOidcUser_AppleExistingSocialUserWithoutEmail() {
		ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("apple")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.clientId("apple-client")
			.authorizationUri("https://appleid.apple.com/auth/authorize")
			.tokenUri("https://appleid.apple.com/auth/token")
			.jwkSetUri("https://appleid.apple.com/auth/keys")
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
			.scope("openid", "email", "name")
			.userInfoUri("https://appleid.apple.com/auth/userinfo")
			.userNameAttributeName("sub")
			.clientName("Apple")
			.build();

		OAuth2AccessToken accessToken = new OAuth2AccessToken(
			OAuth2AccessToken.TokenType.BEARER, "apple-access-token", Instant.now(), Instant.MAX);
		OidcIdToken idToken = new OidcIdToken(
			"apple-id-token",
			Instant.now(),
			Instant.MAX,
			Map.of(
				"sub", "apple-social-id",
				"email_verified", true));

		OidcUserRequest oidcUserRequest = new OidcUserRequest(clientRegistration, accessToken, idToken);
		OidcUser oidcUser = new DefaultOidcUser(Collections.singleton(() -> "ROLE_USER"), idToken, "sub");

		try (MockedConstruction<OidcUserService> mocked = mockConstruction(OidcUserService.class,
			(mock, context) -> when(mock.loadUser(any())).thenReturn(oidcUser))) {
			when(userReader.findBySocialTypeAndSocialId(any(), any())).thenReturn(Optional.of(testUser));

			OidcUser result = customOAuth2UserService.loadOidcUser(oidcUserRequest);

			assertNotNull(result);

			verify(userReader).findBySocialTypeAndSocialId(any(), any());
			verify(userReader, never()).findByEmail(any());
			verify(userWriter, never()).save(any(User.class));
			verify(userWriter, never()).save(any(SocialAccount.class));
		}
	}

	@Test
	@DisplayName("OIDC 애플 로그인에서 email 없이 신규/연동 경로면 예외를 던진다")
	void loadOidcUser_AppleMissingEmailForProvisioning_ThrowsException() {
		ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("apple")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.clientId("apple-client")
			.authorizationUri("https://appleid.apple.com/auth/authorize")
			.tokenUri("https://appleid.apple.com/auth/token")
			.jwkSetUri("https://appleid.apple.com/auth/keys")
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
			.scope("openid", "email", "name")
			.userInfoUri("https://appleid.apple.com/auth/userinfo")
			.userNameAttributeName("sub")
			.clientName("Apple")
			.build();

		OAuth2AccessToken accessToken = new OAuth2AccessToken(
			OAuth2AccessToken.TokenType.BEARER, "apple-access-token", Instant.now(), Instant.MAX);
		OidcIdToken idToken = new OidcIdToken(
			"apple-id-token",
			Instant.now(),
			Instant.MAX,
			Map.of(
				"sub", "apple-social-id",
				"email_verified", true));

		OidcUserRequest oidcUserRequest = new OidcUserRequest(clientRegistration, accessToken, idToken);
		OidcUser oidcUser = new DefaultOidcUser(Collections.singleton(() -> "ROLE_USER"), idToken, "sub");

		try (MockedConstruction<OidcUserService> mocked = mockConstruction(OidcUserService.class,
			(mock, context) -> when(mock.loadUser(any())).thenReturn(oidcUser))) {
			when(userReader.findBySocialTypeAndSocialId(any(), any())).thenReturn(Optional.empty());

			InternalAuthenticationServiceException exception = assertThrows(
				InternalAuthenticationServiceException.class,
				() -> customOAuth2UserService.loadOidcUser(oidcUserRequest));

			assertTrue(exception.getCause() instanceof BaseRunTimeV2Exception);
			BaseRunTimeV2Exception cause = (BaseRunTimeV2Exception)exception.getCause();
			assertEquals(AuthErrorCode.SOCIAL_EMAIL_REQUIRED, cause.getErrorCode());

			verify(userWriter, never()).save(any(User.class));
			verify(userWriter, never()).save(any(SocialAccount.class));
		}
	}
}
