package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum LikePostErrorCode implements BaseResponseCode {
	POST_ALREADY_LIKED(HttpStatus.CONFLICT, "LIKE_POST_409_001", "좋아요를 이미 누른 게시글 입니다."),
	POST_NOT_LIKE(HttpStatus.CONFLICT, "LIKE_POST_409_002", "좋아요을 누르지 않은 게시글입니다.");

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
