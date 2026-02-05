package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum BoardErrorCode implements BaseResponseCode {
	BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_404_001", "게시판을 찾을 수 없습니다"),
	BOARD_NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "BOARD_400_001", "이미 사용 중인 게시판 이름입니다."),
	BOARD_FORBIDDEN(HttpStatus.FORBIDDEN, "BOARD_403_001", "게시판에 대한 권한이 없습니다");

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