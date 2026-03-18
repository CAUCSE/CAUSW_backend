package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;
import net.causw.global.constant.StaticValue;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum UserErrorCode implements BaseResponseCode {
	INVALID_PASSWORD_REQUEST(HttpStatus.BAD_REQUEST, "USER_400_001", "비밀번호 형식이 잘못되었습니다."),
	INVALID_PHONE_NUMBER_REQUEST(HttpStatus.BAD_REQUEST, "USER_400_002", "전화번호 형식이 잘못되었습니다."),
	INVALID_LOGIN_SOCIAL_USER(HttpStatus.BAD_REQUEST, "USER_400_003", "소셜 로그인으로 가입된 계정입니다."),
	INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "USER_401_001", "이메일 또는 비밀번호가 잘못되었습니다."),
	INVALID_LOGIN_USER_DELETED(HttpStatus.FORBIDDEN, "USER_403_001", "삭제된 계정입니다. 회원가입 페이지에서 새로운 정보로 가입해주세요."),
	INVALID_LOGIN_USER_INACTIVE(HttpStatus.FORBIDDEN, "USER_403_002", "탈퇴한 계정입니다. 계정 복구를 진행해주세요"),
	INVALID_LOGIN_USER_DROPPED(HttpStatus.FORBIDDEN, "USER_403_003",
		"추방된 계정입니다. 재가입 문의는 " + StaticValue.ADMIN_EMAIL + "으로 연락해주세요"),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_001", "존재하지 않는 사용자입니다."),
	ALREADY_REGISTERED(HttpStatus.CONFLICT, "USER_409_001", "이미 가입된 계정입니다."),
	USER_INACTIVE_CAN_REJOIN(HttpStatus.CONFLICT, "USER_409_002", "탈퇴한 계정입니다. 계정 복구를 진행해주세요"),
	USER_DROPPED(HttpStatus.CONFLICT, "USER_409_003", "추방된 계정입니다. 재가입 문의는 " + StaticValue.ADMIN_EMAIL + "으로 연락해주세요"),
	EMAIL_ALREADY_EXIST(HttpStatus.CONFLICT, "USER_409_004", "이미 존재하는 이메일입니다."),
	PHONE_NUMBER_ALREADY_EXIST(HttpStatus.CONFLICT, "USER_409_005", "이미 존재하는 전화번호입니다."),
	NICKNAME_ALREADY_EXIST(HttpStatus.CONFLICT, "USER_409_006", "이미 존재하는 닉네임입니다."),
	INVALID_USER_STATE_FOR_ADMISSION(HttpStatus.BAD_REQUEST, "USER_400_003", "가입 대기 또는 거부 상태의 사용자만 인증 신청이 가능합니다."),
	ADMISSION_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_409_007", "이미 인증 신청이 존재합니다."),
	ADMISSION_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "USER_400_004", "증빙서류 이미지는 1개 이상 필수입니다."),
	ADMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_002", "인증 신청을 찾을 수 없습니다."),
	STUDENT_ID_ALREADY_EXIST(HttpStatus.CONFLICT, "USER_409_008", "이미 존재하는 학번입니다."),
	ADMISSION_REJECT_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "USER_400_005", "거절 사유를 입력해 주세요."),
	GRADUATION_YEAR_REQUIRED(HttpStatus.BAD_REQUEST, "USER_400_006", "졸업자는 졸업연도를 입력해 주세요."),
	NICKNAME_SAME_AS_CURRENT(HttpStatus.BAD_REQUEST, "USER_400_007", "현재 닉네임과 동일한 닉네임으로 변경할 수 없습니다."),
	USER_DELETED(HttpStatus.UNAUTHORIZED, "USER_401_004", "삭제된 계정입니다."),
	INVALID_ACADEMIC_STATUS(HttpStatus.BAD_REQUEST, "USER_400_007", "유효하지 않은 학적 상태입니다."),
	INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "USER_400_004", "현재 비밀번호가 일치하지 않습니다."),
	PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "USER_400_005", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."),
	SOCIAL_ONLY_USER_CANNOT_CHANGE_PASSWORD(HttpStatus.BAD_REQUEST, "USER_400_006",
		"소셜 로그인만 연결된 계정은 비밀번호를 변경할 수 없습니다."),
	PASSWORD_RESET_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_400_005", "비밀번호 초기화 인증 유효 시간이 만료되었습니다."),
	PASSWORD_RESET_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_400_006", "비밀번호 초기화 인증 코드가 일치하지 않습니다."),
	USER_NOT_DROPPABLE(HttpStatus.BAD_REQUEST, "USER_400_008", "추방할 수 없는 사용자입니다."),
	USER_NOT_RESTORABLE(HttpStatus.BAD_REQUEST, "USER_400_009", "관리자는 추방 상태의 사용자만 복구할 수 있습니다."),
	USER_ROLE_MISMATCH(HttpStatus.BAD_REQUEST, "USER_400_010", "요청한 현재 역할이 사용자의 역할 목록과 일치하지 않습니다."),
	USER_NOT_ROLE_UPDATABLE(HttpStatus.BAD_REQUEST, "USER_400_011", "활성된 사용자의 역할만 변경할 수 있습니다.");

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
