package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum BlockErrorCode implements BaseResponseCode {
	BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOCK_404_001", "차단 정보를 찾을 수 없습니다"),
	BLOCK_ALREADY_BLOCKED(HttpStatus.CONFLICT, "BLOCK_409_001", "이미 차단한 유저입니다"),
	BLOCK_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "BLOCK_400_001", "본인을 차단할 수 없습니다"),
	BLOCK_FORBIDDEN(HttpStatus.FORBIDDEN, "BLOCK_403_001", "차단에 대한 권한이 없습니다"),
	BLOCK_TARGET_NOT_POST_WRITER(HttpStatus.BAD_REQUEST, "BLOCK_400_002", "차단 대상이 해당 게시글의 작성자가 아닙니다");

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
