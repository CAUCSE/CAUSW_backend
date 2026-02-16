package net.causw.app.main.domain.asset.locker.enums;

import java.util.Arrays;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public enum LockerLogAction {
	ENABLE("ENABLE"),
	DISABLE("DISABLE"),
	REGISTER("REGISTER"),
	RETURN("RETURN"),
	EXTEND("EXTEND"),
	ADMIN_ASSIGN("ADMIN_ASSIGN"),
	ADMIN_EXTEND("ADMIN_EXTEND"),
	ADMIN_RELEASE("ADMIN_RELEASE");

	private final String value;

	LockerLogAction(String value) {
		this.value = value;
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
