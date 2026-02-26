package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum UserInfoErrorCode implements BaseResponseCode {
	TOO_MUCH_SOCIAL_LINK(HttpStatus.BAD_REQUEST, "USERINFO_400_001", "소셜 링크는 최대 10개까지 추가할 수 있습니다."),
	INVALID_ADMISSION_YEAR_RANGE(HttpStatus.BAD_REQUEST, "USERINFO_400_002", "학번 필터 범위가 올바르지 않습니다."),
	USERINFO_NOT_FOUND(HttpStatus.BAD_REQUEST, "USERINFO_404_001", "존재하지 않는 동문 수첩 프로필입니다.");

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
