package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum FileErrorCode implements BaseResponseCode {
	FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_404_001", "파일을 찾을 수 없습니다."),
	FILE_NOT_UPLOADED(HttpStatus.BAD_REQUEST, "FILE_400_001", "파일이 아직 업로드되지 않았습니다."),
	FILE_ALREADY_USED(HttpStatus.BAD_REQUEST, "FILE_400_002", "이미 확정된 파일입니다."),
	INVALID_FILE_PATH(HttpStatus.BAD_REQUEST, "FILE_400_003", "잘못된 파일 경로입니다."),
	FILE_LIST_EMPTY(HttpStatus.BAD_REQUEST, "FILE_400_004", "파일 목록이 비어있습니다."),
	FILE_EXPIRED(HttpStatus.BAD_REQUEST, "FILE_400_005", "파일 업로드 유효 시간이 만료되었습니다. 파일을 다시 업로드해 주세요.");

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
