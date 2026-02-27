package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum NotificationSettingErrorCode implements BaseResponseCode {

	BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_SETTING_404_001", "게시판을 찾을 수 없습니다."),
	BOARD_NOT_NOTICE(HttpStatus.BAD_REQUEST, "NOTIFICATION_SETTING_400_001", "공식계정 게시판이 아닙니다.");

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
