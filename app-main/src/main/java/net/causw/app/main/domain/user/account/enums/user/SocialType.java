package net.causw.app.main.domain.user.account.enums.user;

public enum SocialType {
	GOOGLE,
	APPLE,
	KAKAO;

	public static SocialType from(String registrationId) {
		return SocialType.valueOf(registrationId.toUpperCase());
	}
}
