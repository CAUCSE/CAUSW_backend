package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum CommentErrorCode implements BaseResponseCode {
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404_001", "댓글을 찾을 수 없습니다"),
	COMMENT_ALREADY_LIKED(HttpStatus.CONFLICT, "LIKE_COMMENT_409_001", "좋아요를 이미 누른 댓글 입니다."),
	COMMENT_NOT_LIKE(HttpStatus.CONFLICT, "LIKE_COMMENT_409_002", "좋아요를 누르지 않은 댓글입니다.");

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
