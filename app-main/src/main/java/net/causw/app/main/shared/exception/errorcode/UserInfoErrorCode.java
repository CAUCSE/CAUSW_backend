package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum UserInfoErrorCode implements BaseResponseCode {
	TOO_MUCH_SOCIAL_LINK(HttpStatus.BAD_REQUEST, "USERINFO_400_001", "소셜 링크는 최대 10개까지 추가할 수 있습니다."),
	INVALID_ADMISSION_YEAR_RANGE(HttpStatus.BAD_REQUEST, "USERINFO_400_002", "학번 필터 범위가 올바르지 않습니다."),
	INVALID_CAREER_START_DATE(HttpStatus.BAD_REQUEST, "USERINFO_400_003", "경력 사항 시작 날짜가 올바르지 않습니다."),
	INVALID_CAREER_END_DATE(HttpStatus.BAD_REQUEST, "USERINFO_400_004", "경력 사항 종료 날짜가 올바르지 않습니다."),
	CAREER_START_BEFORE_END(HttpStatus.BAD_REQUEST, "USERINFO_400_005", "경력 사항 시작 날짜는 종료 날짜 이전이어야 합니다."),
	INVALID_PROJECT_START_DATE(HttpStatus.BAD_REQUEST, "USERINFO_400_006", "대표 프로젝트 시작 날짜가 올바르지 않습니다."),
	INVALID_PROJECT_END_DATE(HttpStatus.BAD_REQUEST, "USERINFO_400_007", "대표 프로젝트 종료 날짜가 올바르지 않습니다."),
	PROJECT_START_BEFORE_END(HttpStatus.BAD_REQUEST, "USERINFO_400_008", "대표 프로젝트 시작 날짜는 종료 날짜 이전이어야 합니다."),
	INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST, "USERINFO_400_009", "올바르지 않은 정렬 기준입니다."),
	USERINFO_NOT_FOUND(HttpStatus.NOT_FOUND, "USERINFO_404_001", "존재하지 않는 동문 수첩 프로필입니다."),
	USER_CAREER_NOT_FOUND(HttpStatus.NOT_FOUND, "USERINFO_404_002", "존재하지 않는 경력 사항입니다."),
	USER_PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "USERINFO_404_003", "존재하지 않는 대표 프로젝트입니다.");

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
