package net.causw.app.main.domain.user.auth.service;

import java.util.Optional;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final UserValidator userValidator;

	@Override
	@Transactional
	public CustomOAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		try {
			// 1. 소셜 서비스 구분
			String registrationId = userRequest.getClientRegistration().getRegistrationId();
			// 2. 소셜 고유 식별자 키
			String userNameAttributeName = userRequest.getClientRegistration()
				.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
			// 3. 데이터를 담을 DTO 생성
			OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName,
				oAuth2User.getAttributes());
			// 4. 유저 로그인 또는 회원가입 처리
			User user = saveOrUpdate(attributes);
			userValidator.validateUserStatusForLogin(user.getState());
			// 5. 시큐리티 세션에 담길 유저 객체 반환
			return new CustomOAuth2User(
				user,
				attributes.attributes(),
				attributes.nameAttributeKey());
		} catch (BaseRunTimeV2Exception e) {
			throw new InternalAuthenticationServiceException(e.getMessage(), e);
		}
	}

	private User saveOrUpdate(OAuthAttributes attributes) {
		// 1. 소셜 계정(SocialAccount)이 이미 존재하는지 확인
		Optional<User> existingSocialUser = userReader.findBySocialTypeAndSocialId(attributes.socialType(),
			attributes.socialId());
		if (existingSocialUser.isPresent()) {
			return existingSocialUser.get();
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
			userValidator.validateUserStatusForIntegration(existingUser.getState());
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

}
