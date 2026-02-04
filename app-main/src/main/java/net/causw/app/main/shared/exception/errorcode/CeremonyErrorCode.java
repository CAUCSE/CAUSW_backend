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
	FAMILY_RELATION_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_003", "관계가 가족인 경우, 상세 관계를 입력해야 합니다."),
	ALUMNI_RELATION_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_004", "관계가 동문인 경우, 상세 관계를 입력해야 합니다."),
	ALUMNI_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_005", "관계가 동문인 경우, 동문 이름을 입력해야 합니다."),
	ALUMNI_ADMISSION_YEAR_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_006", "관계가 동문인 경우, 동문 입학 년도 4자리를 입력해야 합니다."),
	END_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_007", "종료 일자를 입력해야 합니다."),
	START_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_008", "시작 시간을 입력해야 합니다."),
	CUSTOM_CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "CEREMONY_400_008", "분류가 직접 입력인 경우, 상세 분류를 직접 입력해야 합니다."),
	CEREMONY_NOT_FOUND(HttpStatus.NOT_FOUND, "CEREMONY_404_001", "해당 경조사를 찾을 수 없습니다."),
	ACCESS_ONLY_APPLICANT(HttpStatus.FORBIDDEN, "CEREMONY_403_001", "본인의 경조사만 조회할 수 있습니다.");

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
