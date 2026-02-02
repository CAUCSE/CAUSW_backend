package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum UserErrorCode implements BaseResponseCode {
	ALREADY_REGISTERED(HttpStatus.CONFLICT, "USER_409_001", "이미 가입된 계정입니다."),
	USER_INACTIVE_CAN_REJOIN(HttpStatus.CONFLICT, "USER_409_002", "탈퇴한 계정입니다. 계정 복구를 진행해주세요"),
	USER_DROPPED(HttpStatus.CONFLICT, "USER_409_003", "추방된 계정입니다. 재가입 문의는 caucsedongne@gmail.com으로 연락해주세요"),
	EMAIL_ALREADY_EXIST(HttpStatus.CONFLICT, "USER_409_004", "이미 존재하는 이메일입니다."),
	PHONE_NUMBER_ALREADY_EXIST(HttpStatus.CONFLICT, "USER_409_005", "이미 존재하는 전화번호입니다."),
	NICKNAME_ALREADY_EXIST(HttpStatus.CONFLICT, "USER_409_006", "이미 존재하는 닉네임입니다.");

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
