package net.causw.app.main.domain.user.auth.service.v2.dto;

import java.util.Map;

import net.causw.app.main.domain.user.account.enums.user.SocialType;

import lombok.Builder;

@Builder
public record OAuthAttributes(
	Map<String, Object> attributes,
	String nameAttributeKey,
	String name,
	String email,
	SocialType socialType,
	String socialId,
	boolean isEmailVerified) {
	public static OAuthAttributes of(String registrationId, String userNameAttributeName,
		Map<String, Object> attributes) {
		SocialType socialType = SocialType.from(registrationId);

		return switch (socialType) {
			case GOOGLE -> ofGoogle(userNameAttributeName, attributes);
			case APPLE -> ofApple(userNameAttributeName, attributes);
			case KAKAO -> ofKakao(userNameAttributeName, attributes);
		};
	}

	private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
		return OAuthAttributes.builder()
			.name((String)attributes.get("name"))
			.email((String)attributes.get("email"))
			.attributes(attributes)
			.nameAttributeKey(userNameAttributeName)
			.socialType(SocialType.GOOGLE)
			.socialId(String.valueOf(attributes.get(userNameAttributeName)))
			.isEmailVerified((boolean)attributes.getOrDefault("email_verified", false))
			.build();
	}

	private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
		Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");

		return OAuthAttributes.builder()
			.name((String)kakaoAccount.get("name"))
			.email((String)kakaoAccount.get("email"))
			.attributes(attributes)
			.nameAttributeKey(userNameAttributeName)
			.socialType(SocialType.KAKAO)
			.socialId(String.valueOf(attributes.get(userNameAttributeName)))
			.isEmailVerified((boolean)kakaoAccount.getOrDefault("is_email_verified", false))
			.build();
	}

	private static OAuthAttributes ofApple(String userNameAttributeName, Map<String, Object> attributes) {
		boolean isVerified = Boolean.parseBoolean(String.valueOf(attributes.getOrDefault("email_verified", "false")));
		// 애플은 이름을 잘 안 주기 때문에 없을 경우 email로 대체
		return OAuthAttributes.builder()
			.name((String)attributes.get("email"))
			.email((String)attributes.get("email"))
			.attributes(attributes)
			.nameAttributeKey(userNameAttributeName)
			.socialType(SocialType.APPLE)
			.socialId(String.valueOf(attributes.get(userNameAttributeName)))
			.isEmailVerified(isVerified)
			.build();
	}
}
