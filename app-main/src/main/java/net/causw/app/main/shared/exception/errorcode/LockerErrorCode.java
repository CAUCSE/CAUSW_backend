package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum LockerErrorCode implements BaseResponseCode {
	LOCKER_NOT_FOUND(HttpStatus.NOT_FOUND, "LOCKER_404_001", "존재하지 않는 사물함입니다."),
	LOCKER_NOT_AVAILABLE(HttpStatus.CONFLICT, "LOCKER_409_001", "사용 가능한 상태의 사물함이 아닙니다."),
	LOCKER_NOT_IN_USE(HttpStatus.CONFLICT, "LOCKER_409_002", "사용중인 사물함이 아닙니다."),
	LOCKER_USER_ALREADY_HAS_LOCKER(HttpStatus.CONFLICT, "LOCKER_409_003", "해당 사용자는 이미 사물함을 사용중입니다."),
	LOCKER_REGISTER_NOT_ALLOWED(HttpStatus.FORBIDDEN, "LOCKER_403_001", "사물함 신청 기간이 아닙니다."),
	LOCKER_IN_USE(HttpStatus.CONFLICT, "LOCKER_409_004", "이미 사용중인 사물함입니다."),
	LOCKER_DISABLED(HttpStatus.CONFLICT, "LOCKER_409_005", "사물함이 사용 불가능한 상태입니다."),
	LOCKER_RETURN_NOT_ALLOWED(HttpStatus.FORBIDDEN, "LOCKER_403_002", "사물함 반납 기간이 아닙니다."),
	LOCKER_EXTEND_NOT_ALLOWED(HttpStatus.FORBIDDEN, "LOCKER_403_003", "사물함 연장 신청 기간이 아닙니다."),
	LOCKER_NOT_OWNER(HttpStatus.FORBIDDEN, "LOCKER_403_004", "해당 사물함의 소유자가 아닙니다."),
	LOCKER_ALREADY_EXTENDED(HttpStatus.CONFLICT, "LOCKER_409_006", "이미 연장된 사물함입니다."),
	LOCKER_ALREADY_ACTIVE(HttpStatus.CONFLICT, "LOCKER_409_007", "이미 활성화된 사물함입니다."),
	LOCKER_ALREADY_DISABLED(HttpStatus.CONFLICT, "LOCKER_409_008", "이미 비활성화된 사물함입니다."),
	LOCKER_EXPIRE_DATE_NOT_SET(HttpStatus.INTERNAL_SERVER_ERROR, "LOCKER_500_001", "사물함 만료일이 설정되지 않았습니다."),
	LOCKER_NEXT_EXPIRE_DATE_NOT_SET(HttpStatus.INTERNAL_SERVER_ERROR, "LOCKER_500_002", "사물함 연장 만료일이 설정되지 않았습니다."),
	LOCKER_REGISTER_PERIOD_NOT_SET(HttpStatus.INTERNAL_SERVER_ERROR, "LOCKER_500_003", "사물함 신청기간이 설정되지 않았습니다."),
	LOCKER_EXTEND_PERIOD_NOT_SET(HttpStatus.INTERNAL_SERVER_ERROR, "LOCKER_500_004", "사물함 연장기간이 설정되지 않았습니다."),
	LOCKER_REGISTER_ALREADY_ACTIVE(HttpStatus.CONFLICT, "LOCKER_409_009", "사물함 신청이 활성화된 상태에서는 연장을 활성화할 수 없습니다."),
	LOCKER_EXTEND_ALREADY_ACTIVE(HttpStatus.CONFLICT, "LOCKER_409_010", "사물함 연장이 활성화된 상태에서는 신청을 활성화할 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}
}
