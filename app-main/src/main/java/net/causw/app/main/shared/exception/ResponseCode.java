package net.causw.app.main.shared.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 전역 응답 코드 HTTP·프레임워크 예외 대응
 */
@Getter
public enum ResponseCode implements BaseResponseCode {
	SUCCESS(HttpStatus.OK, "S000", "요청 처리 성공");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ResponseCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
