package net.causw.app.main.domain.admin.emailcampaign.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

class EmailCampaignTest {

	@Nested
	@DisplayName("발송 상태 전이")
	class TransitionStatus {

		@Test
		@DisplayName("DRAFT 캠페인은 발송 요청 후 QUEUED가 된다")
		void queueDraftCampaign() {
			// given
			EmailCampaign campaign = createCampaign(2);
			LocalDateTime requestedAt = LocalDateTime.of(2026, 9, 7, 12, 0);

			// when
			campaign.queue("admin-id", requestedAt);

			// then
			assertThat(campaign.getStatus()).isEqualTo(EmailCampaignStatus.QUEUED);
			assertThat(campaign.getSendRequestedByUserId()).isEqualTo("admin-id");
			assertThat(campaign.getSendRequestedAt()).isEqualTo(requestedAt);
			assertThat(campaign.getQueuedAt()).isEqualTo(requestedAt);
		}

		@Test
		@DisplayName("DRAFT가 아닌 캠페인은 다시 발송 요청할 수 없다")
		void rejectDuplicateQueue() {
			// given
			EmailCampaign campaign = createCampaign(2);
			campaign.queue("admin-id", LocalDateTime.of(2026, 9, 7, 12, 0));

			// when & then
			assertThatThrownBy(() -> campaign.queue("admin-id", LocalDateTime.of(2026, 9, 7, 12, 1)))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(exception -> ((BaseRunTimeV2Exception)exception).getErrorCode())
				.isEqualTo(EmailCampaignErrorCode.EMAIL_CAMPAIGN_SEND_ALREADY_REQUESTED);
		}

		@Test
		@DisplayName("수신자 집계에 따라 부분 실패 상태로 종료한다")
		void completeAsPartiallyFailed() {
			// given
			EmailCampaign campaign = createCampaign(2);
			campaign.queue("admin-id", LocalDateTime.of(2026, 9, 7, 12, 0));
			campaign.start(LocalDateTime.of(2026, 9, 7, 12, 1));

			// when
			campaign.complete(1, 1, 0, LocalDateTime.of(2026, 9, 7, 12, 2));

			// then
			assertThat(campaign.getStatus()).isEqualTo(EmailCampaignStatus.PARTIALLY_FAILED);
			assertThat(campaign.getPendingCount()).isZero();
			assertThat(campaign.getSentCount()).isEqualTo(1);
			assertThat(campaign.getFailedCount()).isEqualTo(1);
		}
	}

	private EmailCampaign createCampaign(long recipientCount) {
		return EmailCampaign.of(
			"행사 안내",
			"<p>행사 안내</p>",
			"noreply@example.com",
			"reply@example.com",
			"{}",
			recipientCount,
			"creator-id");
	}
}
