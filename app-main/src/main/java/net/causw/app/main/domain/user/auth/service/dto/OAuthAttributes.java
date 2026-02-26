package net.causw.app.main.domain.user.auth.service.dto;

import java.util.Map;

import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

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
		String socialId = resolveAppleSocialId(userNameAttributeName, attributes);
		String email = getString(attributes, "email");
		boolean isVerified = Boolean.parseBoolean(String.valueOf(attributes.getOrDefault("email_verified", "false")));
		String name = getString(attributes, "name");
		if (!hasText(name) && hasText(email)) {
			name = email;
		}

		return OAuthAttributes.builder()
			.name(name)
			.email(email)
			.attributes(attributes)
			.nameAttributeKey(userNameAttributeName)
			.socialType(SocialType.APPLE)
			.socialId(socialId)
			.isEmailVerified(isVerified)
			.build();
	}

	private static String resolveAppleSocialId(String userNameAttributeName, Map<String, Object> attributes) {
		String socialId = getString(attributes, userNameAttributeName);
		if (!hasText(socialId)) {
			socialId = getString(attributes, "sub");
		}
		if (!hasText(socialId)) {
			throw AuthErrorCode.INVALID_SOCIAL_IDENTIFIER.toBaseException();
		}

		return socialId;
	}

	private static String getString(Map<String, Object> attributes, String key) {
		Object value = attributes.get(key);
		if (value == null) {
			return null;
		}
		String text = String.valueOf(value);
		return hasText(text) ? text : null;
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
