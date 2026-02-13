package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum AuthErrorCode implements BaseResponseCode {

	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_001", "유효하지 않은 토큰입니다"),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_002", "유효하지 않은 리프레시토큰입니다"),
	REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH_401_003", "토큰 값이 존재하지 않습니다.");

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
