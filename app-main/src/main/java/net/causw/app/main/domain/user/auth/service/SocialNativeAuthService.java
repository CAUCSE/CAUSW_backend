package net.causw.app.main.domain.user.auth.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.dto.CustomOAuth2User;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
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

	private static final long PROVIDER_TOKEN_DEFAULT_TTL_SECONDS = 300L;
	private static final String OIDC_SUB_CLAIM = "sub";
	private static final String APPLE_ISSUER = "https://appleid.apple.com";
	private static final String GOOGLE_ISSUER = "https://accounts.google.com";

	private final ClientRegistrationRepository clientRegistrationRepository;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final JwtDecoderFactory<ClientRegistration> oidcIdTokenDecoderFactory;
	private final AuthTokenManager authTokenManager;

	/**
	 * provider 특성에 따라 access token 또는 id token 기반 네이티브 소셜 로그인을 수행합니다.
	 *
	 * @param provider provider registration id (예: kakao, google, apple)
	 * @param accessToken 네이티브 SDK에서 발급된 provider access token
	 * @param idToken OIDC provider(google/apple)에서 발급된 id token
	 * @return 서비스 전용 access/refresh token이 포함된 인증 결과
	 */
	@Transactional
	public AuthResult login(String provider, String accessToken, String idToken) {
		String registrationId = provider.toLowerCase(Locale.ROOT);
		log.info("Native social login requested. provider={}, hasIdToken={}", registrationId, hasText(idToken));

		try {
			SocialType.from(registrationId);

			ClientRegistration clientRegistration = Optional.ofNullable(
				clientRegistrationRepository.findByRegistrationId(registrationId))
				.orElseThrow(AuthErrorCode.UNSUPPORTED_SOCIAL_PROVIDER::toBaseException);

			User user = loadAuthenticatedUser(clientRegistration, accessToken, idToken);
			AuthTokenPair tokens = authTokenManager.issueTokens(user, null);

			log.info("Native social login succeeded. provider={}, userId={}", registrationId, user.getId());

			return AuthResult.of(tokens.accessToken(), user.getName(), user.getEmail(), user.getProfileUrl(),
				tokens.refreshToken(), user.getState());
		} catch (BaseRunTimeV2Exception e) {
			log.warn("Native social login failed. provider={}, code={}, message={}", registrationId,
				e.getErrorCode().getCode(), e.getMessage());
			throw e;
		} catch (RuntimeException e) {
			log.error("Unexpected native social login error. provider={}", registrationId, e);
			throw e;
		}
	}

	/**
	 * provider 설정(openid scope 여부)에 따라 OAuth2(access token) 또는 OIDC(id token) 검증 경로를 선택합니다.
	 */
	private User loadAuthenticatedUser(ClientRegistration clientRegistration, String providerAccessToken,
		String providerIdToken) {
		if (isOidcProvider(clientRegistration)) {
			return loadOidcAuthenticatedUser(clientRegistration, providerIdToken);
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
			toProviderAccessToken(providerAccessToken));

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
	private User loadOidcAuthenticatedUser(ClientRegistration clientRegistration, String providerIdToken) {
		log.debug("Verifying OIDC id token and loading user. provider={}", clientRegistration.getRegistrationId());

		String normalizedIdToken = normalizeIdToken(providerIdToken);
		if (!hasText(normalizedIdToken)) {
			log.warn("OIDC provider requested without id token. provider={}", clientRegistration.getRegistrationId());
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}

		Jwt jwt = decodeOidcIdToken(clientRegistration, normalizedIdToken);
		validateOidcClaims(clientRegistration, jwt);

		try {
			return customOAuth2UserService.loadUserFromOidcClaims(clientRegistration.getRegistrationId(),
				jwt.getClaims());
		} catch (BaseRunTimeV2Exception e) {
			log.warn("OIDC user mapping failed. provider={}, code={}, message={}",
				clientRegistration.getRegistrationId(), e.getErrorCode().getCode(), e.getMessage());
			throw e;
		} catch (RuntimeException e) {
			log.warn("OIDC user mapping failed unexpectedly. provider={}, exceptionType={}",
				clientRegistration.getRegistrationId(), e.getClass().getSimpleName());
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}
	}

	/**
	 * provider별 OIDC 설정으로 id token을 디코딩합니다.
	 * 디코딩 과정에서 서명 및 기본 JWT 검증이 수행됩니다.
	 */
	private Jwt decodeOidcIdToken(ClientRegistration clientRegistration, String idToken) {
		try {
			JwtDecoder decoder = oidcIdTokenDecoderFactory.createDecoder(clientRegistration);
			return decoder.decode(idToken);
		} catch (RuntimeException e) {
			log.warn("OIDC id token decode failed. provider={}, exceptionType={}",
				clientRegistration.getRegistrationId(), e.getClass().getSimpleName());
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}
	}

	/**
	 * OIDC id token의 필수 claim을 검증합니다.
	 * <ul>
	 * <li>sub: 소셜 사용자 식별자 존재 여부</li>
	 * <li>aud: 현재 client_id 포함 여부</li>
	 * <li>iss: provider별 expected issuer 일치 여부</li>
	 * </ul>
	 */
	private void validateOidcClaims(ClientRegistration clientRegistration, Jwt jwt) {
		String subject = jwt.getClaimAsString(OIDC_SUB_CLAIM);
		if (!hasText(subject)) {
			throw AuthErrorCode.INVALID_SOCIAL_IDENTIFIER.toBaseException();
		}

		List<String> audience = jwt.getAudience();
		if (audience == null || audience.isEmpty() || !audience.contains(clientRegistration.getClientId())) {
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}

		String expectedIssuer = resolveExpectedIssuer(clientRegistration);
		if (hasText(expectedIssuer)
			&& (jwt.getIssuer() == null || !expectedIssuer.equals(jwt.getIssuer().toString()))) {
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}
	}

	/**
	 * issuer 검증 기준값을 반환합니다.
	 * provider 설정의 issuerUri를 우선 사용하고, 없으면 provider별 기본값으로 보완합니다.
	 */
	private String resolveExpectedIssuer(ClientRegistration clientRegistration) {
		String issuerUri = clientRegistration.getProviderDetails().getIssuerUri();
		if (hasText(issuerUri)) {
			return issuerUri;
		}

		String registrationId = clientRegistration.getRegistrationId();
		if ("apple".equalsIgnoreCase(registrationId)) {
			return APPLE_ISSUER;
		}

		if ("google".equalsIgnoreCase(registrationId)) {
			return GOOGLE_ISSUER;
		}

		return null;
	}

	private OAuth2AccessToken toProviderAccessToken(String accessToken) {
		String normalizedAccessToken = normalizeAccessToken(accessToken);
		if (!hasText(normalizedAccessToken)) {
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plusSeconds(PROVIDER_TOKEN_DEFAULT_TTL_SECONDS);
		return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, normalizedAccessToken, issuedAt, expiresAt);
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

	private boolean isOidcProvider(ClientRegistration clientRegistration) {
		return clientRegistration.getScopes().contains("openid");
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	/**
	 * access token 입력값을 정규화합니다.
	 * Bearer 접두어가 포함된 경우 제거합니다.
	 */
	private String normalizeAccessToken(String accessToken) {
		if (!hasText(accessToken)) {
			return accessToken;
		}

		String trimmed = accessToken.trim();
		if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return trimmed.substring(7).trim();
		}

		return trimmed;
	}

	/**
	 * id token 입력값을 정규화합니다.
	 * Bearer 접두어가 포함된 경우 제거합니다.
	 */
	private String normalizeIdToken(String idToken) {
		if (!hasText(idToken)) {
			return idToken;
		}

		String trimmed = idToken.trim();
		if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return trimmed.substring(7).trim();
		}

		return trimmed;
	}
}
