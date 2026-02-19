package net.causw.app.main.domain.asset.locker.enums;

import java.util.Arrays;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.Getter;

public enum LockerLogAction {
	ENABLE("ENABLE", "사물함 활성화"),
	DISABLE("DISABLE", "사물함 비활성화"),
	REGISTER("REGISTER", "사물함 신청"),
	RETURN("RETURN", "사물함 반납"),
	EXTEND("EXTEND", "사물함 연장"),
	ADMIN_ASSIGN("ADMIN_ASSIGN", "관리자 사물함 배정"),
	ADMIN_EXTEND("ADMIN_EXTEND", "관리자 사물함 연장"),
	ADMIN_RELEASE("ADMIN_RELEASE", "관리자 사물함 회수");

	private final String value;
	@Getter
	private final String description;

	LockerLogAction(String value, String description) {
		this.value = value;
		this.description = description;
	}

	public static LockerLogAction of(String value) {
		return Arrays.stream(values())
			.filter(v -> value.equalsIgnoreCase(v.value))
			.findFirst()
			.orElseThrow(
				() -> new BadRequestException(
					ErrorCode.INVALID_REQUEST_ROLE,
					String.format("'%s' is invalid : not supported", value)));
	}
}
