package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum CommentReportErrorCode implements BaseResponseCode {
	COMMENT_REPORT_ALREADY_REPORTED(HttpStatus.CONFLICT, "COMMENT_REPORT_409_001", "이미 신고한 댓글입니다"),
	COMMENT_REPORT_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COMMENT_REPORT_400_001", "본인 댓글은 신고할 수 없습니다");

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
