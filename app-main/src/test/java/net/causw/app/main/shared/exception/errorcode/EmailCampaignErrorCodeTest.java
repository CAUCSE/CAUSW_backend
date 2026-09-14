package net.causw.app.main.shared.exception.errorcode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class EmailCampaignErrorCodeTest {

	@Test
	@DisplayName("이메일 캠페인 오류 코드는 API 계약에 맞는 상태와 식별 코드를 제공한다")
	void provideStatusAndCodeByContract() {
		assertThat(EmailCampaignErrorCode.EMAIL_CAMPAIGN_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(EmailCampaignErrorCode.EMAIL_CAMPAIGN_NOT_FOUND.getCode())
			.isEqualTo("EMAIL_CAMPAIGN_404_001");
		assertThat(EmailCampaignErrorCode.EMAIL_CAMPAIGN_RECIPIENT_EMPTY.getStatus())
			.isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_FILTER.getStatus())
			.isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_HTML.getStatus())
			.isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(EmailCampaignErrorCode.EMAIL_CAMPAIGN_CONFIRMATION_MISMATCH.getStatus())
			.isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_STATUS.getStatus())
			.isEqualTo(HttpStatus.CONFLICT);
		assertThat(EmailCampaignErrorCode.EMAIL_CAMPAIGN_SEND_ALREADY_REQUESTED.getStatus())
			.isEqualTo(HttpStatus.CONFLICT);
		assertThat(EmailCampaignErrorCode.EMAIL_CAMPAIGN_SES_DISABLED.getStatus())
			.isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(EmailCampaignErrorCode.EMAIL_CAMPAIGN_SES_CONFIGURATION_INVALID.getStatus())
			.isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
	}
}
