package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum AuthErrorCode implements BaseResponseCode {

	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_001", "유효하지 않은 토큰입니다"),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_002", "유효하지 않은 리프레시토큰입니다"),
	REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH_401_003", "토큰 값이 존재하지 않습니다."),
	USER_ROLE_NONE(HttpStatus.UNAUTHORIZED, "AUTH_401_004", "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.");

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
