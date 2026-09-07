package net.causw.app.main.domain.admin.emailcampaign.entity;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;
import net.causw.app.main.shared.entity.AuditableEntity;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_email_campaign_recipient", uniqueConstraints = {
	@UniqueConstraint(name = "uk_email_campaign_recipient_campaign_user", columnNames = {"campaign_id", "user_id"})
})
public class EmailCampaignRecipient extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "campaign_id", nullable = false)
	private EmailCampaign campaign;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "email_snapshot", nullable = false)
	private String emailSnapshot;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private EmailCampaignRecipientStatus status;

	@Column(name = "ses_message_id")
	private String sesMessageId;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Column(name = "last_attempt_at")
	private LocalDateTime lastAttemptAt;

	@Column(name = "next_attempt_at")
	private LocalDateTime nextAttemptAt;

	@Column(name = "claimed_at")
	private LocalDateTime claimedAt;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@Column(name = "last_error_code", length = 100)
	private String lastErrorCode;

	@Column(name = "last_error_detail", length = 500)
	private String lastErrorDetail;

	@Column(name = "skip_reason", length = 100)
	private String skipReason;

	public static EmailCampaignRecipient of(EmailCampaign campaign, String userId, String emailSnapshot) {
		return EmailCampaignRecipient.builder()
			.campaign(campaign)
			.userId(userId)
			.emailSnapshot(emailSnapshot)
			.status(EmailCampaignRecipientStatus.PENDING)
			.attemptCount(0)
			.build();
	}

	public void claim(LocalDateTime now) {
		validateStatus(EmailCampaignRecipientStatus.PENDING);
		status = EmailCampaignRecipientStatus.SENDING;
		attemptCount++;
		lastAttemptAt = now;
		claimedAt = now;
		nextAttemptAt = null;
	}

	public void markSent(String messageId, LocalDateTime now) {
		validateStatus(EmailCampaignRecipientStatus.SENDING);
		status = EmailCampaignRecipientStatus.SENT;
		sesMessageId = messageId;
		sentAt = now;
		claimedAt = null;
		clearError();
	}

	public void markRetryableFailure(String errorCode, String errorDetail, LocalDateTime nextAttemptAt) {
		validateStatus(EmailCampaignRecipientStatus.SENDING);
		status = EmailCampaignRecipientStatus.PENDING;
		lastErrorCode = errorCode;
		lastErrorDetail = errorDetail;
		this.nextAttemptAt = nextAttemptAt;
		claimedAt = null;
	}

	public void markPermanentFailure(String errorCode, String errorDetail) {
		validateStatus(EmailCampaignRecipientStatus.SENDING);
		status = EmailCampaignRecipientStatus.FAILED;
		lastErrorCode = errorCode;
		lastErrorDetail = errorDetail;
		claimedAt = null;
		nextAttemptAt = null;
	}

	public void markSkipped(String reason) {
		validateStatus(EmailCampaignRecipientStatus.PENDING);
		status = EmailCampaignRecipientStatus.SKIPPED;
		skipReason = reason;
		nextAttemptAt = null;
	}

	public void releaseStaleClaim(LocalDateTime nextAttemptAt) {
		validateStatus(EmailCampaignRecipientStatus.SENDING);
		status = EmailCampaignRecipientStatus.PENDING;
		claimedAt = null;
		this.nextAttemptAt = nextAttemptAt;
	}

	private void clearError() {
		lastErrorCode = null;
		lastErrorDetail = null;
	}

	private void validateStatus(EmailCampaignRecipientStatus expected) {
		if (status != expected) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_STATUS.toBaseException();
		}
	}
}
