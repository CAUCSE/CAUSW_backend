package net.causw.app.main.domain.user.auth.service;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
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

@Service
@Slf4j
@RequiredArgsConstructor
/**
 * 소셜 제공자의 access token을 검증하고, 서비스 전용 JWT를 발급하는 네이티브 소셜 로그인 서비스입니다.
 * <p>
 * 처리 흐름:
 * 1) provider 타입을 검증하고 client registration을 조회합니다.
 * 2) provider access token으로 {@link OAuth2UserRequest}를 구성합니다.
 * 3) {@link CustomOAuth2UserService}를 재사용해 provider 사용자 정보를 도메인 사용자로 매핑합니다.
 * 4) {@link AuthTokenManager}를 통해 CAUSW access/refresh token을 발급합니다.
 *
 * <p>
 * 참고: 이 서비스는 네이티브 환경의 access-token-only 플로우를 대상으로 합니다.
 * ID Token 검증이 필요한 OIDC provider는 이 플로우에서 허용하지 않습니다.
 */
public class SocialNativeAuthService {

	private static final long PROVIDER_TOKEN_DEFAULT_TTL_SECONDS = 300L;

	private final ClientRegistrationRepository clientRegistrationRepository;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final AuthTokenManager authTokenManager;

	/**
	 * provider access token 검증 기반의 네이티브 소셜 로그인을 수행합니다.
	 *
	 * @param provider provider registration id (예: kakao, google, apple)
	 * @param accessToken 네이티브 SDK에서 발급된 provider access token
	 * @return 서비스 전용 access/refresh token이 포함된 인증 결과
	 */
	@Transactional
	public AuthResult login(String provider, String accessToken) {
		String registrationId = provider.toLowerCase(Locale.ROOT);
		log.info("Native social login requested. provider={}", registrationId);

		try {
			SocialType.from(registrationId);

			ClientRegistration clientRegistration = Optional.ofNullable(
				clientRegistrationRepository.findByRegistrationId(registrationId))
				.orElseThrow(AuthErrorCode.UNSUPPORTED_SOCIAL_PROVIDER::toBaseException);

			User user = loadAuthenticatedUser(clientRegistration, accessToken);
			AuthTokenPair tokens = authTokenManager.issueTokens(user, null);

			log.info("Native social login succeeded. provider={}, userId={}", registrationId, user.getId());

			return AuthResult.of(tokens.accessToken(), user.getName(), user.getEmail(), user.getProfileUrl(),
				tokens.refreshToken());
		} catch (BaseRunTimeV2Exception e) {
			log.warn("Native social login failed. provider={}, code={}, message={}", registrationId,
				e.getErrorCode().getCode(), e.getMessage());
			throw e;
		} catch (RuntimeException e) {
			log.error("Unexpected native social login error. provider={}", registrationId, e);
			throw e;
		}
	}

	private User loadAuthenticatedUser(ClientRegistration clientRegistration, String providerAccessToken) {
		log.debug("Verifying provider access token and loading user. provider={}",
			clientRegistration.getRegistrationId());

		if (isOidcProvider(clientRegistration)) {
			log.warn("Native access-token-only flow is unsupported for OIDC provider. provider={}",
				clientRegistration.getRegistrationId());
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}

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

	private OAuth2AccessToken toProviderAccessToken(String accessToken) {
		String normalizedAccessToken = normalizeAccessToken(accessToken);
		if (!hasText(normalizedAccessToken)) {
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plusSeconds(PROVIDER_TOKEN_DEFAULT_TTL_SECONDS);
		return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, normalizedAccessToken, issuedAt, expiresAt);
	}

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
}
