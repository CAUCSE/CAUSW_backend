package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum AcademicRecordApplicationErrorCode implements BaseResponseCode {
	ACADEMIC_RECORD_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "ACADEMIC_APPLICATION_404_001",
		"학적 변경 신청서를 찾을 수 없습니다."),
	ACADEMIC_RECORD_APPLICATION_NOT_AWAITING(HttpStatus.BAD_REQUEST, "ACADEMIC_APPLICATION_400_001",
		"대기 상태인 신청서만 승인/반려할 수 있습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public HttpStatus getStatus() {
		return this.status;
	}
}
