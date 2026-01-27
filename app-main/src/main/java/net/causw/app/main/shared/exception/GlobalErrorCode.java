package net.causw.app.main.shared.exception;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum GlobalErrorCode implements BaseResponseCode {

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G50001", "서버 내부 오류가 발생했습니다"),
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "G40001", "잘못된 요청입니다"),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "G40101", "인증이 필요합니다"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "G40301", "권한이 없습니다");

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
