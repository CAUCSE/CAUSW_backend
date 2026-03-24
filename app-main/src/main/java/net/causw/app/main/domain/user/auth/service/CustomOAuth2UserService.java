package net.causw.app.main.domain.user.auth.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.dto.CustomOAuth2User;
import net.causw.app.main.domain.user.auth.service.dto.OAuthAttributes;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * OAuth2/OIDC 소셜 로그인 사용자 정보를 애플리케이션의 User/SocialAccount로 매핑하고,
 * 기존 계정 연동 또는 신규 계정 생성을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final UserValidator userValidator;

	/**
	 * OAuth2 UserInfo 기반 로그인 요청을 처리합니다.
	 * <p>
	 * provider별 속성을 {@link OAuthAttributes}로 표준화한 뒤,
	 * 계정 조회/연동/생성을 수행하고 인증 principal로 사용할 {@link CustomOAuth2User}를 반환합니다.
	 *
	 * @param userRequest OAuth2 공급자에서 전달된 사용자 요청 정보
	 * @return 인증 컨텍스트에 저장할 커스텀 OAuth2 사용자 객체
	 * @throws OAuth2AuthenticationException 인증 처리 중 도메인 예외가 발생한 경우
	 */
	@Override
	@Transactional
	public CustomOAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		try {
			// 1. 소셜 서비스 구분
			String registrationId = userRequest.getClientRegistration().getRegistrationId();
			// 2. 소셜 고유 식별자 키
			String userNameAttributeName = resolveUserNameAttributeName(
				userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
					.getUserNameAttributeName());
			User user = processSocialLogin(registrationId, userNameAttributeName, oAuth2User.getAttributes());

			return new CustomOAuth2User(
				user,
				oAuth2User.getAttributes(),
				userNameAttributeName);
		} catch (BaseRunTimeV2Exception e) {
			throw new InternalAuthenticationServiceException(e.getMessage(), e);
		}
	}

	/**
	 * OIDC(UserInfo/ID Token claim) 기반 로그인 요청을 처리합니다.
	 * <p>
	 * claim 정보를 도메인 사용자로 동기화한 뒤, Spring Security 표준 {@link OidcUser}를 그대로 반환합니다.
	 *
	 * @param userRequest OIDC 공급자에서 전달된 사용자 요청 정보
	 * @return 인증 컨텍스트에 저장할 OIDC 사용자 객체
	 * @throws OAuth2AuthenticationException 인증 처리 중 도메인 예외가 발생한 경우
	 */
	@Transactional
	public OidcUser loadOidcUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OidcUserRequest, OidcUser> delegate = new OidcUserService();
		OidcUser oidcUser = delegate.loadUser(userRequest);

		try {
			String registrationId = userRequest.getClientRegistration().getRegistrationId();
			loadUserFromOidcClaims(registrationId, oidcUser.getClaims());
			return oidcUser;
		} catch (BaseRunTimeV2Exception e) {
			throw new InternalAuthenticationServiceException(e.getMessage(), e);
		}
	}

	/**
	 * 네이티브 OIDC(id_token) 기반 요청처럼 claim만 전달되는 경우,
	 * claim을 사용자 도메인과 동기화하여 로그인 가능한 User를 반환합니다.
	 */
	@Transactional
	public User loadUserFromOidcClaims(String registrationId, Map<String, Object> oidcClaims) {
		return processSocialLogin(registrationId, resolveUserNameAttributeName("sub"), oidcClaims);
	}

	private User processSocialLogin(String registrationId, String userNameAttributeName,
		Map<String, Object> attributesMap) {
		OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, attributesMap);
		User user = saveOrUpdate(attributes);
		userValidator.validateUserStatusForLogin(user);
		return user;
	}

	private String resolveUserNameAttributeName(String userNameAttributeName) {
		if (userNameAttributeName == null || userNameAttributeName.isBlank()) {
			return "sub";
		}

		return userNameAttributeName;
	}

	private User saveOrUpdate(OAuthAttributes attributes) {
		// 1. 소셜 계정(SocialAccount)이 이미 존재하는지 확인
		Optional<User> existingSocialUser = userReader.findBySocialTypeAndSocialId(attributes.socialType(),
			attributes.socialId());
		if (existingSocialUser.isPresent()) {
			return existingSocialUser.get();
		}

		if (!hasText(attributes.email())) {
			throw AuthErrorCode.SOCIAL_EMAIL_REQUIRED.toBaseException();
		}

		// 2. 소셜 계정은 없지만, 같은 이메일을 가진 기존 유저(User)가 있는지 확인
		Optional<User> existingEmailUser = userReader.findByEmail(attributes.email());
		if (existingEmailUser.isPresent()) {
			User existingUser = existingEmailUser.get();

			// 2-1. 기존 유저가 있다면 소셜 계정만 새로 연결 (계정 통합)
			if (!attributes.isEmailVerified()) {
				throw AuthErrorCode.UNVERIFIED_SOCIAL_EMAIL.toBaseException();
			}
			userValidator.checkAccountExistByUserAndSocialType(existingUser, attributes.socialType());
			userValidator.validateUserStatusForIntegration(existingUser);
			createAndSaveSocialAccount(attributes, existingUser);

			return existingUser;
		}

		// 2-2. 소셜 계정도 없고 이메일 중복도 없는 경우 (GUEST)
		User newUser = User.createSocialUser(attributes);
		userWriter.save(newUser);
		createAndSaveSocialAccount(attributes, newUser);

		return newUser;
	}

	private void createAndSaveSocialAccount(OAuthAttributes attributes, User user) {
		SocialAccount socialAccount = SocialAccount.of(
			attributes.socialType(),
			attributes.socialId(),
			attributes.email(),
			user);
		userWriter.save(socialAccount);
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

}
