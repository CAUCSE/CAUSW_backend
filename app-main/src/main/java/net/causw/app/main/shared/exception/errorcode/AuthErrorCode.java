package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;
import net.causw.global.constant.StaticValue;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum AuthErrorCode implements BaseResponseCode {

	UNSUPPORTED_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_400_001", "지원하지 않는 소셜 로그인입니다."),
	INVALID_REGISTRATION_STATUS(HttpStatus.BAD_REQUEST, "AUTH_400_002", "현재 계정 상태로는 수행할 수 없는 요청입니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_001", "유효하지 않은 토큰입니다"),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_002", "유효하지 않은 리프레시토큰입니다"),
	REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH_401_003", "토큰 값이 존재하지 않습니다."),
	USER_ROLE_NONE(HttpStatus.UNAUTHORIZED, "AUTH_401_005", "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요."),
	DROPPED_USER(HttpStatus.UNAUTHORIZED, "AUTH_401_006",
		"추방된 계정입니다. 재가입 문의는 " + StaticValue.ADMIN_EMAIL + "으로 연락해주세요"),
	INACTIVE_USER(HttpStatus.UNAUTHORIZED, "AUTH_401_007", "탈퇴한 계정입니다. 계정 복구를 진행해주세요"),
	DELETED_USER(HttpStatus.UNAUTHORIZED, "AUTH_401_008", "삭제된 계정입니다. 회원가입 페이지에서 새로운 정보로 가입해주세요."),
	NO_PERMISSION_FOR_RESOURCE(HttpStatus.FORBIDDEN, "AUTH_403_001", "해당 자원에 대한 접근 또는 조작 권한이 없습니다."),
	UNVERIFIED_SOCIAL_EMAIL(HttpStatus.FORBIDDEN, "AUTH_403_002", "소셜로그인 계정의 이메일이 인증되지 않았습니다."),
	ALREADY_LINKED_SOCIAL_PROVIDER(HttpStatus.CONFLICT, "AUTH_409_001", "하나의 계정에 소셜로그인 별 하나의 계정만 연동 가능합니다.");

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
