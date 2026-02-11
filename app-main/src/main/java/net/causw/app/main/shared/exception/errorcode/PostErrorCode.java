package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum PostErrorCode implements BaseResponseCode {
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_404_001", "게시글을 찾을 수 없습니다"),
	POST_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_403_001", "게시글에 대한 권한이 없습니다"),
	POST_ANONYMOUS_BOARD_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "POST_400_001", "익명 게시판에는 비익명 게시글을 작성할 수 없습니다");

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
