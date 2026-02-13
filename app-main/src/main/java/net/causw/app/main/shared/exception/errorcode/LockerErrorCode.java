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
	LOCKER_USER_ALREADY_HAS_LOCKER(HttpStatus.CONFLICT, "LOCKER_409_003", "해당 사용자는 이미 사물함을 사용중입니다.");

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
