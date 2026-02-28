package net.causw.app.main.domain.user.account.enums.user;

import java.util.Arrays;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserState {
	AWAIT("AWAIT", "가입 대기"),
	ACTIVE("ACTIVE", "활성"),
	REJECT("REJECT", "가입 거부"),
	DROP("DROP", "추방"),
	GUEST("GUEST", "소셜로그인 대기자");

	private final String value;
	private final String description;

	public static UserState of(String value) {
		return Arrays.stream(values())
			.filter(v -> value.equalsIgnoreCase(v.value))
			.findFirst()
			.orElseThrow(
				() -> new BadRequestException(
					ErrorCode.INVALID_REQUEST_USER_STATE,
					String.format("'%s' is invalid : not supported", value)));
	}
}
