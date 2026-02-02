package net.causw.app.main.domain.user.account.enums.user;

import java.util.Arrays;

import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserState {
	AWAIT("AWAIT", "가입 대기"),
	ACTIVE("ACTIVE", "활성"),
	INACTIVE("INACTIVE", "탈퇴"),
	REJECT("REJECT", "가입 거부"),
	DROP("DROP", "추방"),
	DELETED("DELETED", "삭제됨");

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

	// 사용자가 가입 (재가입)이 가능한지 확인하는 메서드
	public void validateSignupPossible() {
		switch (this) {
			case ACTIVE, AWAIT, REJECT ->
				throw UserErrorCode.ALREADY_REGISTERED.toBaseException();
			case DROP ->
				throw UserErrorCode.USER_DROPPED.toBaseException();
			case INACTIVE ->
				throw UserErrorCode.USER_INACTIVE_CAN_REJOIN.toBaseException();
			default -> {}
		}
	}

    public void validateLoginPossible() {
        switch (this) {
            case DELETED ->
                    throw UserErrorCode.INVALID_LOGIN_USER_DELETED.toBaseException();
            case DROP ->
                    throw UserErrorCode.INVALID_LOGIN_USER_DROPPED.toBaseException();
            case INACTIVE ->
                    throw UserErrorCode.INVALID_LOGIN_USER_INACTIVE.toBaseException();
            default -> {}
        }
    }
}
