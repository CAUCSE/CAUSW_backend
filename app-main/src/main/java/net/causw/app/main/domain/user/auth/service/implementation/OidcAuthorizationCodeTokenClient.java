package net.causw.app.main.domain.user.auth.service.implementation;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * OIDC/OAuth2 공급자 토큰 엔드포인트에 authorization code grant로 요청해 리프레시 토큰을 획득합니다.
 */
@Component
@Slf4j
public class OidcAuthorizationCodeTokenClient {

	private final ObjectMapper objectMapper;
	private final RestClient restClient;

	public OidcAuthorizationCodeTokenClient(ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
		this.objectMapper = objectMapper;
		this.restClient = restClientBuilder.build();
	}

	/**
	 * @param codeVerifier PKCE 사용 시 필수, 미사용 시 null
	 * @return 응답에 포함된 refresh_token. 없으면 null (예: Google 재동의 시 미반환)
	 */
	public String exchangeAuthorizationCode(ClientRegistration registration, String authorizationCode,
		String codeVerifier) {
		if (!StringUtils.hasText(authorizationCode)) {
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("code", authorizationCode.trim());
		String redirectUri = registration.getRedirectUri();
		if (StringUtils.hasText(redirectUri)) {
			form.add("redirect_uri", redirectUri.trim());
		}
		form.add("client_id", registration.getClientId());
		if (StringUtils.hasText(registration.getClientSecret())) {
			form.add("client_secret", registration.getClientSecret());
		}
		if (StringUtils.hasText(codeVerifier)) {
			form.add("code_verifier", codeVerifier.trim());
		}

		String tokenUri = registration.getProviderDetails().getTokenUri();
		try {
			String raw = restClient.post()
				.uri(tokenUri)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.APPLICATION_JSON)
				.body(form)
				.retrieve()
				.body(String.class);

			if (!StringUtils.hasText(raw)) {
				log.warn("Empty token response from provider. registrationId={}", registration.getRegistrationId());
				throw AuthErrorCode.INVALID_TOKEN.toBaseException();
			}

			@SuppressWarnings("unchecked") Map<String, Object> body = objectMapper.readValue(raw, Map.class);
			Object error = body.get("error");
			if (error != null) {
				log.warn("Token endpoint error. registrationId={}, error={}, description={}",
					registration.getRegistrationId(), error, body.get("error_description"));
				throw AuthErrorCode.INVALID_TOKEN.toBaseException();
			}
			Object refreshToken = body.get("refresh_token");
			if (refreshToken == null) {
				return null;
			}
			String token = String.valueOf(refreshToken);
			return StringUtils.hasText(token) ? token : null;
		} catch (RestClientResponseException e) {
			log.warn("Token endpoint HTTP error. registrationId={}, status={}", registration.getRegistrationId(),
				e.getStatusCode(), e);
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		} catch (BaseRunTimeV2Exception e) {
			throw e;
		} catch (Exception e) {
			log.warn("Token exchange failed. registrationId={}", registration.getRegistrationId(), e);
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}
	}

}
