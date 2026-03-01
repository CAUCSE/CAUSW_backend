package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum PostReportErrorCode implements BaseResponseCode {
	POST_REPORT_ALREADY_REPORTED(HttpStatus.CONFLICT, "POST_REPORT_409_001", "이미 신고한 게시글입니다"),
	POST_REPORT_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "POST_REPORT_400_001", "본인 게시글은 신고할 수 없습니다");

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
