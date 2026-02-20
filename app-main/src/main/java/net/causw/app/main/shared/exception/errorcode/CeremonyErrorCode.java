package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum CeremonyErrorCode implements BaseResponseCode {
	TARGET_ADMISSION_YEARS_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_001",
		"모두에게 알림을 전송하지 않을 경우, 알림을 전송할 대상 학번을 입력해야 합니다."),
	INVALID_ADMISSION_YEARS_FORMAT(HttpStatus.BAD_REQUEST, "CEREMONY_400_002",
		"학번은 숫자 두 자리로 입력해야 합니다. (ex. 72, 05, 21)"),
	INVALID_RELATION(HttpStatus.BAD_REQUEST, "CEREMONY_400_003", "관계에 맞게 상세 관계가 입력되어야 합니다."),
	FAMILY_RELATION_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_004", "관계가 가족인 경우, 상세 관계를 입력해야 합니다."),
	ALUMNI_RELATION_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_005", "관계가 동문인 경우, 상세 관계를 입력해야 합니다."),
	ALUMNI_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_006", "관계가 동문인 경우, 동문 이름을 입력해야 합니다."),
	ALUMNI_ADMISSION_YEAR_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_007", "관계가 동문인 경우, 동문 입학 년도 4자리를 입력해야 합니다."),
	END_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_008", "종료 일자를 입력해야 합니다."),
	START_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_009", "시작 시간을 입력해야 합니다."),
	END_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_010", "종료 시간을 입력해야 합니다."),
	DATETIME_BEFORE_TODAY(HttpStatus.BAD_REQUEST, "CEREMONY_400_011", "이미 끝난 경조사는 신청할 수 없습니다."),
	CUSTOM_CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_012", "분류가 직접 입력인 경우, 상세 분류를 직접 입력해야 합니다."),
	CUSTOM_CATEGORY_NOT_NULL(HttpStatus.BAD_REQUEST, "CEREMONY_400_013", "분류가 직접 입력이 아닌 경우, 상세 분류는 Null이어야 합니다."),
	DATETIME_END_AFTER_START(HttpStatus.BAD_REQUEST, "CEREMONY_400_014", "시작 일시는 종료 일시 이전이어야 합니다."),
	INVALID_CEREMONY_CONTEXT(HttpStatus.BAD_REQUEST, "CEREMONY_400_015", "잘못된 context입니다."),
	INVALID_CEREMONY_TYPE(HttpStatus.BAD_REQUEST, "CEREMONY_400_016", "잘못된 type입니다."),
	INVALID_CEREMONY_STATE(HttpStatus.BAD_REQUEST, "CEREMONY_400_017", "잘못된 state입니다."),
	ACCESS_ONLY_APPLICANT(HttpStatus.FORBIDDEN, "CEREMONY_403_001", "본인의 경조사만 조회할 수 있습니다."),
	CEREMONY_NOT_FOUND(HttpStatus.NOT_FOUND, "CEREMONY_404_001", "해당 경조사를 찾을 수 없습니다.");

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
