package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum EmailCampaignErrorCode implements BaseResponseCode {
	EMAIL_CAMPAIGN_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL_CAMPAIGN_404_001", "이메일 캠페인을 찾을 수 없습니다."),
	EMAIL_CAMPAIGN_RECIPIENT_EMPTY(HttpStatus.BAD_REQUEST, "EMAIL_CAMPAIGN_400_001", "이메일 캠페인 발송 대상이 없습니다."),
	EMAIL_CAMPAIGN_INVALID_FILTER(HttpStatus.BAD_REQUEST, "EMAIL_CAMPAIGN_400_002", "이메일 캠페인 발송 대상 조건이 올바르지 않습니다."),
	EMAIL_CAMPAIGN_INVALID_HTML(HttpStatus.BAD_REQUEST, "EMAIL_CAMPAIGN_400_003", "이메일 본문 HTML이 올바르지 않습니다."),
	EMAIL_CAMPAIGN_CONFIRMATION_MISMATCH(HttpStatus.BAD_REQUEST, "EMAIL_CAMPAIGN_400_004",
		"최종 확인 정보가 캠페인 정보와 일치하지 않습니다."),
	EMAIL_CAMPAIGN_INVALID_SUBJECT(HttpStatus.BAD_REQUEST, "EMAIL_CAMPAIGN_400_005", "이메일 제목이 올바르지 않습니다."),
	EMAIL_CAMPAIGN_INVALID_STATUS(HttpStatus.CONFLICT, "EMAIL_CAMPAIGN_409_001", "현재 상태에서는 이메일 캠페인 작업을 수행할 수 없습니다."),
	EMAIL_CAMPAIGN_SEND_ALREADY_REQUESTED(HttpStatus.CONFLICT, "EMAIL_CAMPAIGN_409_002", "이미 발송 요청된 이메일 캠페인입니다."),
	EMAIL_CAMPAIGN_SES_DISABLED(HttpStatus.SERVICE_UNAVAILABLE, "EMAIL_CAMPAIGN_503_001",
		"이메일 캠페인 발송 기능이 비활성화되어 있습니다."),
	EMAIL_CAMPAIGN_SES_CONFIGURATION_INVALID(HttpStatus.SERVICE_UNAVAILABLE, "EMAIL_CAMPAIGN_503_002",
		"이메일 캠페인 발송 설정이 올바르지 않습니다.");

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
