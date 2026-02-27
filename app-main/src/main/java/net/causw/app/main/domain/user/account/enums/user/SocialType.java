package net.causw.app.main.domain.user.account.enums.user;

import java.util.Arrays;

import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

public enum SocialType {
	GOOGLE,
	APPLE,
	KAKAO;

	public static SocialType from(String registrationId) {
		return Arrays.stream(SocialType.values())
			.filter(type -> type.name().equalsIgnoreCase(registrationId))
			.findFirst()
			.orElseThrow(AuthErrorCode.UNSUPPORTED_SOCIAL_PROVIDER::toBaseException);
	}
}
