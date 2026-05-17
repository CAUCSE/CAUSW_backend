package net.causw.app.main.domain.user.auth.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.service.v2.implementation.UserProfileImageReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.dto.CustomOAuth2User;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.implementation.OAuthAttributesResolver;
import net.causw.app.main.domain.user.auth.service.implementation.OidcAuthorizationCodeTokenClient;
import net.causw.app.main.domain.user.auth.service.implementation.SocialAccountOauthRefreshStore;
import net.causw.app.main.domain.user.terms.service.implementation.UserTermsAgreementReader;
import net.causw.app.main.shared.dto.ProfileImageDto;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 소셜 제공자의 access token 또는 OIDC id token을 검증하고,
 * 서비스 전용 JWT를 발급하는 네이티브 소셜 로그인 서비스입니다.
 * <p>
 * 처리 흐름:
 * 1) provider 타입을 검증하고 client registration을 조회합니다.
 * 2) 일반 OAuth2 provider는 access token 기반으로 user-info를 검증합니다.
 * 3) OIDC provider(google/apple)는 id token을 검증하고 claim 기반으로 사용자 동기화를 수행합니다.
 * 4) {@link AuthTokenManager}를 통해 CAUSW access/refresh token을 발급합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SocialNativeAuthService {

	private final ClientRegistrationRepository clientRegistrationRepository;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final AuthTokenManager authTokenManager;
	private final UserTermsAgreementReader userTermsAgreementReader;
	private final UserProfileImageReader userProfileImageReader;
	private final OidcAuthorizationCodeTokenClient oidcAuthorizationCodeTokenClient;
	private final SocialAccountOauthRefreshStore socialAccountOauthRefreshStore;
	private final OAuthAttributesResolver oAuthAttributesResolver;

	/**
	 * provider 특성에 따라 access token 또는 id token 기반 네이티브 소셜 로그인을 수행합니다.
	 *
	 * @param provider provider registration id (예: kakao, google, apple)
	 * @param accessToken 네이티브 SDK에서 발급된 provider access token
	 * @param idToken OIDC provider(google/apple)에서 발급된 id token
	 * @param authorizationCode OIDC 인가 코드(선택). 전달 시 토큰 엔드포인트로 교환해 리프레시 토큰을 저장합니다.
	 * @param codeVerifier PKCE 사용 시 code_verifier(선택)
	 * @return 서비스 전용 access/refresh token이 포함된 인증 결과
	 */
	@Transactional
	public AuthResult login(String provider, String platform, String accessToken, String idToken,
		String authorizationCode, String codeVerifier) {
		String providerKey = provider.toLowerCase(Locale.ROOT);
		SocialType socialType = SocialType.from(providerKey);
		String oidcRegistrationId = resolveRegistrationId(provider, platform);
		log.info(
			"Native social login requested. provider={}, platform={}, oidcRegistrationId={}, hasIdToken={}, hasAuthCode={}",
			provider, platform, oidcRegistrationId, StringUtils.hasText(idToken),
			StringUtils.hasText(authorizationCode));

		try {
			ClientRegistration clientRegistration = Optional.ofNullable(
				clientRegistrationRepository.findByRegistrationId(oidcRegistrationId))
				.orElseThrow(AuthErrorCode.UNSUPPORTED_SOCIAL_PROVIDER::toBaseException);

			User user = loadAuthenticatedUser(socialType, clientRegistration, accessToken, idToken);
			if (oAuthAttributesResolver.isOidcProvider(clientRegistration)) {
				ClientRegistration tokenExchangeRegistration = resolveOidcTokenExchangeRegistration(socialType,
					clientRegistration);
				persistOidcRefreshTokenFromAuthorizationCode(tokenExchangeRegistration, socialType, user,
					authorizationCode, codeVerifier);
			}
			AuthTokenPair tokens = authTokenManager.issueTokens(user, null);
			UserProfileImage profileImage = userProfileImageReader.findByUserIdOrNull(user.getId());
			boolean hasAllRequiredLatestTerms = userTermsAgreementReader.hasAgreedToAllRequiredLatestTerms(user);

			log.info("Native social login succeeded. provider={}, oidcRegistrationId={}, userId={}", provider,
				oidcRegistrationId, user.getId());

			return AuthResult.of(tokens.accessToken(), user.getName(), user.getEmail(),
				ProfileImageDto.from(user, profileImage),
				tokens.refreshToken(), user.isGuest(), hasAllRequiredLatestTerms, user.isAcademicCertified(),
				user.getAcademicStatus());
		} catch (BaseRunTimeV2Exception e) {
			log.warn("Native social login failed. provider={}, code={}, message={}", providerKey,
				e.getErrorCode().getCode(), e.getMessage());
			throw e;
		} catch (RuntimeException e) {
			log.error("Unexpected native social login error. provider={}", providerKey, e);
			throw e;
		}
	}

	/**
	 * provider 설정(openid scope 여부)에 따라 OAuth2(access token) 또는 OIDC(id token) 검증 경로를 선택합니다.
	 */
	private User loadAuthenticatedUser(SocialType socialType, ClientRegistration clientRegistration,
		String providerAccessToken, String providerIdToken) {
		if (oAuthAttributesResolver.isOidcProvider(clientRegistration)) {
			return loadOidcAuthenticatedUser(socialType, clientRegistration, providerIdToken);
		}

		return loadOAuth2AuthenticatedUser(clientRegistration, providerAccessToken);
	}

	/**
	 * OAuth2 provider(Kakao 등)에서 access token으로 user-info를 조회해 사용자 인증/동기화를 수행합니다.
	 */
	private User loadOAuth2AuthenticatedUser(ClientRegistration clientRegistration, String providerAccessToken) {
		log.debug("Verifying provider access token and loading user. provider={}",
			clientRegistration.getRegistrationId());

		OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration,
			oAuthAttributesResolver.toProviderAccessToken(providerAccessToken));

		try {
			CustomOAuth2User customOAuth2User = customOAuth2UserService.loadUser(userRequest);
			return customOAuth2User.user();
		} catch (InternalAuthenticationServiceException e) {
			RuntimeException resolvedException = resolveAuthException(e);
			if (resolvedException instanceof BaseRunTimeV2Exception baseException) {
				log.warn("OAuth2 user mapping failed. provider={}, code={}, message={}",
					clientRegistration.getRegistrationId(), baseException.getErrorCode().getCode(),
					baseException.getMessage());
			} else {
				log.warn("OAuth2 user mapping failed. provider={}, message={}",
					clientRegistration.getRegistrationId(), resolvedException.getMessage());
			}
			throw resolvedException;
		} catch (RuntimeException e) {
			log.warn("Provider access token verification failed. provider={}, exceptionType={}",
				clientRegistration.getRegistrationId(), e.getClass().getSimpleName());
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}
	}

	/**
	 * OIDC provider(Google/Apple)에서 id token을 디코딩/검증하고 claim 기반 사용자 동기화를 수행합니다.
	 */
	private User loadOidcAuthenticatedUser(SocialType socialType, ClientRegistration clientRegistration,
		String providerIdToken) {
		log.debug("Verifying OIDC id token and loading user. registrationId={}",
			clientRegistration.getRegistrationId());

		Jwt jwt = oAuthAttributesResolver.decodeAndValidateOidcIdToken(clientRegistration, providerIdToken);

		try {
			return customOAuth2UserService.loadUserFromOidcClaims(socialType.registrationId(), jwt.getClaims());
		} catch (BaseRunTimeV2Exception e) {
			log.warn("OIDC user mapping failed. registrationId={}, code={}, message={}",
				clientRegistration.getRegistrationId(), e.getErrorCode().getCode(), e.getMessage());
			throw e;
		} catch (RuntimeException e) {
			log.warn("OIDC user mapping failed unexpectedly. registrationId={}, exceptionType={}",
				clientRegistration.getRegistrationId(), e.getClass().getSimpleName());
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}
	}

	private String resolveRegistrationId(String provider, String platform) {
		String normalizedProvider = normalizeKey(provider);
		if (!StringUtils.hasText(normalizedProvider)) {
			return provider;
		}
		if ("google".equals(normalizedProvider)) {
			return "google";
		}

		String normalizedPlatform = normalizeKey(platform);
		if ("apple".equals(normalizedProvider)) {
			if (!StringUtils.hasText(normalizedPlatform)) {
				return "apple-ios";
			}
			ClientRegistration appleRegistration = clientRegistrationRepository.findByRegistrationId(
				"apple-" + normalizedPlatform);
			return appleRegistration != null ? appleRegistration.getRegistrationId() : "apple";
		}

		return normalizedProvider;
	}

	private String normalizeKey(String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		return raw.trim().toLowerCase(Locale.ROOT);
	}

	/**
	 * OAuth 인증 중 래핑된 예외에서 도메인 예외를 복원합니다.
	 * 복원할 수 없으면 INVALID_TOKEN으로 통일합니다.
	 */
	private RuntimeException resolveAuthException(RuntimeException exception) {
		Throwable cause = exception.getCause();
		if (cause instanceof BaseRunTimeV2Exception baseRunTimeV2Exception) {
			return baseRunTimeV2Exception;
		}
		return AuthErrorCode.INVALID_TOKEN.toBaseException();
	}

	private ClientRegistration resolveOidcTokenExchangeRegistration(SocialType socialType,
		ClientRegistration oidcClientRegistration) {
		if (socialType == SocialType.GOOGLE) {
			return Optional
				.ofNullable(clientRegistrationRepository.findByRegistrationId(SocialType.GOOGLE.registrationId()))
				.orElseThrow(AuthErrorCode.UNSUPPORTED_SOCIAL_PROVIDER::toBaseException);
		}
		return oidcClientRegistration;
	}

	private void persistOidcRefreshTokenFromAuthorizationCode(ClientRegistration registration, SocialType socialType,
		User user, String authorizationCode, String codeVerifier) {
		if (!StringUtils.hasText(authorizationCode)) {
			return;
		}
		String refreshToken = oidcAuthorizationCodeTokenClient.exchangeAuthorizationCode(registration,
			authorizationCode, codeVerifier);
		if (!StringUtils.hasText(refreshToken)) {
			log.info("Native OIDC token exchange returned no refresh_token; cipher not updated. provider={}",
				registration.getRegistrationId());
			return;
		}
		socialAccountOauthRefreshStore.saveEncryptedRefreshToken(user.getId(), socialType, refreshToken);
	}

}
