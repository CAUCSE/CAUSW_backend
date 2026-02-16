package net.causw.app.main.domain.user.auth.service.v2;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.v2.dto.OAuthAttributes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final UserValidator userValidator;

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);
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
		return new DefaultOAuth2User(
			Collections.singleton(new SimpleGrantedAuthority(user.getState().name())),
			attributes.attributes(),
			attributes.nameAttributeKey());
	}

	private User saveOrUpdate(OAuthAttributes attributes) {
		// 1. 소셜 계정(SocialAccount)이 이미 존재하는지 확인
		return userReader.findBySocialTypeAndSocialId(attributes.socialType(), attributes.socialId())
			.orElseGet(() -> {
				// 2. 소셜 계정은 없지만, 같은 이메일을 가진 기존 유저(User)가 있는지 확인
				return userReader.findByEmail(attributes.email())
					.map(existingUser -> {
						// 2-1. 기존 유저가 있다면 소셜 계정만 새로 연결 (계정 통합)
						userValidator.validateUserStatusForSignup(existingUser.getState());
						createAndSaveSocialAccount(attributes, existingUser);
						return existingUser;
					})
					.orElseGet(() -> {
						// 2-2. 소셜 계정도 없고 이메일 중복도 없는 경우 (GUEST)
						User newUser = User.createSocialUser(attributes);
						userWriter.save(newUser);
						createAndSaveSocialAccount(attributes, newUser);
						return newUser;
					});
			});
	}

	private void createAndSaveSocialAccount(OAuthAttributes attributes, User user) {
		//TODO: 유저 프로필 처리 필요
		SocialAccount socialAccount = SocialAccount.from(
			attributes.socialType(),
			attributes.socialId(),
			attributes.email(),
			user);
		userWriter.save(socialAccount);
	}

}
