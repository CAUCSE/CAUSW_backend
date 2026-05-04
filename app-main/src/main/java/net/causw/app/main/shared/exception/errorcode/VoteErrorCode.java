package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum VoteErrorCode implements BaseResponseCode {
	VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE_404_001", "투표를 찾을 수 없습니다"),
	VOTE_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE_404_002", "투표 옵션을 찾을 수 없습니다"),
	VOTE_ALREADY_END(HttpStatus.BAD_REQUEST, "VOTE_400_001", "이미 종료된 투표입니다"),
	VOTE_NOT_END(HttpStatus.BAD_REQUEST, "VOTE_400_002", "아직 종료되지 않은 투표입니다"),
	VOTE_OPTION_NOT_IN_VOTE(HttpStatus.BAD_REQUEST, "VOTE_400_003", "해당 옵션이 투표에 속하지 않습니다"),
	VOTE_CREATE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "VOTE_403_001", "투표를 생성할 권한이 없습니다"),
	VOTE_END_NOT_ALLOWED(HttpStatus.FORBIDDEN, "VOTE_403_002", "투표를 종료할 권한이 없습니다"),
	VOTE_RESTART_NOT_ALLOWED(HttpStatus.FORBIDDEN, "VOTE_403_003", "투표를 재시작할 권한이 없습니다");

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
