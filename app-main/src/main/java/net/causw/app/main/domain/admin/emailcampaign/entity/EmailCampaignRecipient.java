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

	/**
	 * 캠페인 생성 시점의 사용자와 이메일을 보존하는 PENDING 수신자를 생성한다.
	 * @param campaign 소속 캠페인
	 * @param userId 수신 사용자 ID
	 * @param emailSnapshot 생성 시점 이메일
	 * @return 초기 수신자
	 */
	public static EmailCampaignRecipient of(EmailCampaign campaign, String userId, String emailSnapshot) {
		return EmailCampaignRecipient.builder()
			.campaign(campaign)
			.userId(userId)
			.emailSnapshot(emailSnapshot)
			.status(EmailCampaignRecipientStatus.PENDING)
			.attemptCount(0)
			.build();
	}

	/**
	 * PENDING 수신자를 SENDING으로 claim하고 시도 횟수를 증가시킨다.
	 * @param now claim 시각
	 */
	public void claim(LocalDateTime now) {
		validateStatus(EmailCampaignRecipientStatus.PENDING);
		status = EmailCampaignRecipientStatus.SENDING;
		attemptCount++;
		lastAttemptAt = now;
		claimedAt = now;
		nextAttemptAt = null;
	}

	/**
	 * SES 접수 결과를 기록하고 수신자를 SENT로 전환한다.
	 * @param messageId SES 메시지 ID
	 * @param now 발송 완료 시각
	 */
	public void markSent(String messageId, LocalDateTime now) {
		validateStatus(EmailCampaignRecipientStatus.SENDING);
		status = EmailCampaignRecipientStatus.SENT;
		sesMessageId = messageId;
		sentAt = now;
		claimedAt = null;
		clearError();
	}

	/**
	 * 재시도 가능한 실패를 기록하고 지정 시각 이후 처리할 PENDING 상태로 되돌린다.
	 * @param errorCode 안전한 실패 코드
	 * @param errorDetail 개인정보가 없는 실패 설명
	 * @param nextAttemptAt 다음 시도 시각
	 */
	public void markRetryableFailure(String errorCode, String errorDetail, LocalDateTime nextAttemptAt) {
		validateStatus(EmailCampaignRecipientStatus.SENDING);
		status = EmailCampaignRecipientStatus.PENDING;
		lastErrorCode = errorCode;
		lastErrorDetail = errorDetail;
		this.nextAttemptAt = nextAttemptAt;
		claimedAt = null;
	}

	/**
	 * 재시도하지 않는 실패를 기록하고 수신자를 FAILED로 전환한다.
	 * @param errorCode 안전한 실패 코드
	 * @param errorDetail 개인정보가 없는 실패 설명
	 */
	public void markPermanentFailure(String errorCode, String errorDetail) {
		validateStatus(EmailCampaignRecipientStatus.SENDING);
		status = EmailCampaignRecipientStatus.FAILED;
		lastErrorCode = errorCode;
		lastErrorDetail = errorDetail;
		claimedAt = null;
		nextAttemptAt = null;
	}

	/**
	 * 발송 대상에서 제외된 사유를 기록하고 SKIPPED로 전환한다.
	 * @param reason 제외 사유
	 */
	public void markSkipped(String reason) {
		validateStatus(EmailCampaignRecipientStatus.PENDING);
		status = EmailCampaignRecipientStatus.SKIPPED;
		skipReason = reason;
		nextAttemptAt = null;
	}

	/**
	 * timeout된 SENDING claim을 다음 처리가 가능한 PENDING 상태로 되돌린다.
	 * @param nextAttemptAt 다음 시도 가능 시각
	 */
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
