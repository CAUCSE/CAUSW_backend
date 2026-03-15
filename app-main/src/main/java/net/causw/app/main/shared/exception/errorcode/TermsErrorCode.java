package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum TermsErrorCode implements BaseResponseCode {
	TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS_404_001", "해당 이용약관을 찾을 수 없습니다."),
	NOT_ALL_TERMS_AGREED(HttpStatus.BAD_REQUEST, "TERMS_400_001", "전체 약관에 모두 동의해야 합니다.");

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
