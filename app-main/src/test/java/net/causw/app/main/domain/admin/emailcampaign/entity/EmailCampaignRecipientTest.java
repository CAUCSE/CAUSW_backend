package net.causw.app.main.domain.admin.emailcampaign.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;

class EmailCampaignRecipientTest {

	@Test
	@DisplayName("수신자를 claim하면 SENDING 상태가 되고 시도 횟수가 증가한다")
	void claimPendingRecipient() {
		// given
		EmailCampaignRecipient recipient = EmailCampaignRecipient.of(null, "user-id", "user@example.com");
		LocalDateTime claimedAt = LocalDateTime.of(2026, 9, 7, 12, 0);

		// when
		recipient.claim(claimedAt);

		// then
		assertThat(recipient.getStatus()).isEqualTo(EmailCampaignRecipientStatus.SENDING);
		assertThat(recipient.getAttemptCount()).isEqualTo(1);
		assertThat(recipient.getClaimedAt()).isEqualTo(claimedAt);
		assertThat(recipient.getLastAttemptAt()).isEqualTo(claimedAt);
	}

	@Test
	@DisplayName("재시도 가능한 실패는 다음 시도 시각과 함께 PENDING으로 돌아간다")
	void markRetryableFailure() {
		// given
		EmailCampaignRecipient recipient = EmailCampaignRecipient.of(null, "user-id", "user@example.com");
		recipient.claim(LocalDateTime.of(2026, 9, 7, 12, 0));
		LocalDateTime nextAttemptAt = LocalDateTime.of(2026, 9, 7, 12, 5);

		// when
		recipient.markRetryableFailure("THROTTLING", "발송률 제한", nextAttemptAt);

		// then
		assertThat(recipient.getStatus()).isEqualTo(EmailCampaignRecipientStatus.PENDING);
		assertThat(recipient.getNextAttemptAt()).isEqualTo(nextAttemptAt);
		assertThat(recipient.getClaimedAt()).isNull();
		assertThat(recipient.getLastErrorCode()).isEqualTo("THROTTLING");
	}

	@Test
	@DisplayName("SES가 접수하면 메시지 ID와 완료 시각을 저장한다")
	void markSent() {
		// given
		EmailCampaignRecipient recipient = EmailCampaignRecipient.of(null, "user-id", "user@example.com");
		recipient.claim(LocalDateTime.of(2026, 9, 7, 12, 0));
		LocalDateTime sentAt = LocalDateTime.of(2026, 9, 7, 12, 1);

		// when
		recipient.markSent("ses-message-id", sentAt);

		// then
		assertThat(recipient.getStatus()).isEqualTo(EmailCampaignRecipientStatus.SENT);
		assertThat(recipient.getSesMessageId()).isEqualTo("ses-message-id");
		assertThat(recipient.getSentAt()).isEqualTo(sentAt);
		assertThat(recipient.getClaimedAt()).isNull();
	}
}
