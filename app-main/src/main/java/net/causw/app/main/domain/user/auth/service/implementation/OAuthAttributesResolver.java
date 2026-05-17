package net.causw.app.main.domain.user.auth.service.implementation;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.auth.service.dto.OAuthAttributes;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 소셜 provider의 access token 또는 OIDC id token을 검증하고 표준화된 {@link OAuthAttributes}를 추출하는 컴포넌트입니다.
 * <p>
 * 네이티브 로그인과 소셜 계정 연동 등 다양한 진입점에서 동일한 토큰 검증/속성 추출 로직을 공유하기 위해 분리했습니다.
 * 사용자 동기화/JWT 발급 등의 상위 흐름은 호출 측 서비스에서 담당합니다.
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OAuthAttributesResolver {

	private static final long PROVIDER_TOKEN_DEFAULT_TTL_SECONDS = 300L;
	private static final String OIDC_SUB_CLAIM = "sub";
	private static final String APPLE_ISSUER = "https://appleid.apple.com";
	private static final String GOOGLE_ISSUER = "https://accounts.google.com";

	private final ClientRegistrationRepository clientRegistrationRepository;
	private final JwtDecoderFactory<ClientRegistration> oidcIdTokenDecoderFactory;

	/**
	 * provider 토큰을 검증하고 소셜 계정 속성만 반환합니다. 계정 생성/연동은 수행하지 않습니다.
	 * 마이페이지 소셜 계정 연동 시 토큰 유효성 검증 및 socialId/socialType 추출 용도로 사용합니다.
	 *
	 * @param provider    provider registration id (kakao, google, apple)
	 * @param accessToken 카카오용 access token
	 * @param idToken     구글/애플용 id token
	 * @return 표준화된 소셜 계정 속성 (socialId, socialType, email 포함)
	 */
	public OAuthAttributes resolveAttributes(String provider, String accessToken, String idToken) {
		String registrationId = provider.toLowerCase(Locale.ROOT);
		log.info("Social account link token extraction requested. provider={}", registrationId);

		try {
			SocialType.from(registrationId);

			ClientRegistration clientRegistration = Optional.ofNullable(
				clientRegistrationRepository.findByRegistrationId(registrationId))
				.orElseThrow(AuthErrorCode.UNSUPPORTED_SOCIAL_PROVIDER::toBaseException);

			if (isOidcProvider(clientRegistration)) {
				return extractOidcAttributes(clientRegistration, registrationId, idToken);
			}

			return extractOAuth2Attributes(clientRegistration, registrationId, accessToken);
		} catch (BaseRunTimeV2Exception e) {
			log.warn("Social account link token extraction failed. provider={}, code={}, message={}",
				registrationId, e.getErrorCode().getCode(), e.getMessage());
			throw e;
		} catch (RuntimeException e) {
			log.error("Unexpected error during social account link token extraction. provider={}", registrationId, e);
			throw e;
		}
	}

	/**
	 * provider 설정의 scope에 openid가 포함되어 있는지로 OIDC 여부를 판별합니다.
	 */
	public boolean isOidcProvider(ClientRegistration clientRegistration) {
		return clientRegistration.getScopes().contains("openid");
	}

	/**
	 * OIDC id token을 디코딩하고 필수 claim(sub/aud/iss)을 검증한 뒤 {@link Jwt}를 반환합니다.
	 */
	public Jwt decodeAndValidateOidcIdToken(ClientRegistration clientRegistration, String idToken) {
		String normalizedIdToken = normalizeAccessToken(idToken);
		if (!StringUtils.hasText(normalizedIdToken)) {
			log.warn("OIDC provider requested without id token. registrationId={}",
				clientRegistration.getRegistrationId());
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}
		Jwt jwt = decodeOidcIdToken(clientRegistration, normalizedIdToken);
		validateOidcClaims(clientRegistration, jwt);
		return jwt;
	}

	/**
	 * provider access token 문자열을 정규화한 뒤 Spring의 {@link OAuth2AccessToken} 형태로 감쌉니다.
	 * Bearer 접두어가 있으면 제거하고, 빈 값이면 INVALID_TOKEN 예외를 발생시킵니다.
	 */
	public OAuth2AccessToken toProviderAccessToken(String accessToken) {
		String normalizedAccessToken = normalizeAccessToken(accessToken);
		if (!StringUtils.hasText(normalizedAccessToken)) {
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plusSeconds(PROVIDER_TOKEN_DEFAULT_TTL_SECONDS);
		return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, normalizedAccessToken, issuedAt, expiresAt);
	}

	private OAuthAttributes extractOidcAttributes(ClientRegistration clientRegistration, String registrationId,
		String idToken) {
		Jwt jwt = decodeAndValidateOidcIdToken(clientRegistration, idToken);
		return OAuthAttributes.of(registrationId, OIDC_SUB_CLAIM, jwt.getClaims());
	}

	private OAuthAttributes extractOAuth2Attributes(ClientRegistration clientRegistration, String registrationId,
		String accessToken) {
		OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration,
			toProviderAccessToken(accessToken));
		try {
			// provider user-info API에 HTTP 요청을 보내서 액세스 토큰 유효성 검증
			DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
			OAuth2User oAuth2User = delegate.loadUser(userRequest);
			String userNameAttributeName = clientRegistration.getProviderDetails()
				.getUserInfoEndpoint().getUserNameAttributeName();
			return OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
		} catch (RuntimeException e) {
			log.warn("OAuth2 attributes extraction failed. provider={}, exceptionType={}, message={}",
				registrationId, e.getClass().getSimpleName(), e.getMessage());
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}
	}

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

	private void validateOidcClaims(ClientRegistration clientRegistration, Jwt jwt) {
		String subject = jwt.getClaimAsString(OIDC_SUB_CLAIM);
		if (!StringUtils.hasText(subject)) {
			throw AuthErrorCode.INVALID_SOCIAL_IDENTIFIER.toBaseException();
		}

		List<String> audience = jwt.getAudience();
		if (!isAudienceAllowed(clientRegistration, audience)) {
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}

		String expectedIssuer = resolveExpectedIssuer(clientRegistration);
		if (StringUtils.hasText(expectedIssuer)
			&& (jwt.getIssuer() == null || !expectedIssuer.equals(jwt.getIssuer().toString()))) {
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}
	}

	private boolean isAudienceAllowed(ClientRegistration clientRegistration, List<String> tokenAudience) {
		if (tokenAudience == null || tokenAudience.isEmpty()) {
			return false;
		}

		String clientId = clientRegistration.getClientId();
		for (String aud : tokenAudience) {
			if (StringUtils.hasText(aud) && aud.equals(clientId)) {
				return true;
			}
		}
		return false;
	}

	private String resolveExpectedIssuer(ClientRegistration clientRegistration) {
		String issuerUri = clientRegistration.getProviderDetails().getIssuerUri();
		if (StringUtils.hasText(issuerUri)) {
			return issuerUri;
		}

		String registrationId = clientRegistration.getRegistrationId();
		if (registrationId != null && registrationId.toLowerCase(Locale.ROOT).startsWith("apple")) {
			return APPLE_ISSUER;
		}

		if (SocialType.GOOGLE.matchesRegistrationId(registrationId)) {
			return GOOGLE_ISSUER;
		}

		return null;
	}

	private String normalizeAccessToken(String accessToken) {
		if (!StringUtils.hasText(accessToken)) {
			return accessToken;
		}

		String trimmed = accessToken.trim();
		if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return trimmed.substring(7).trim();
		}

		return trimmed;
	}
}
