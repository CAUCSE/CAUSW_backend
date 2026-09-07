package net.causw.app.main.domain.admin.emailcampaign.entity;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;
import net.causw.app.main.shared.entity.BaseEntity;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
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
@Table(name = "tb_email_campaign")
public class EmailCampaign extends BaseEntity {

	@Column(name = "subject", nullable = false)
	private String subject;

	@Lob
	@Column(name = "sanitized_html", nullable = false, columnDefinition = "LONGTEXT")
	private String sanitizedHtml;

	@Column(name = "from_address", nullable = false)
	private String fromAddress;

	@Column(name = "reply_to_address")
	private String replyToAddress;

	@Column(name = "filter_json", nullable = false, columnDefinition = "JSON")
	private String filterJson;

	@Column(name = "recipient_count", nullable = false)
	private long recipientCount;

	@Column(name = "pending_count", nullable = false)
	private long pendingCount;

	@Column(name = "sent_count", nullable = false)
	private long sentCount;

	@Column(name = "failed_count", nullable = false)
	private long failedCount;

	@Column(name = "skipped_count", nullable = false)
	private long skippedCount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private EmailCampaignStatus status;

	@Column(name = "created_by_user_id", nullable = false)
	private String createdByUserId;

	@Column(name = "send_requested_by_user_id")
	private String sendRequestedByUserId;

	@Column(name = "send_requested_at")
	private LocalDateTime sendRequestedAt;

	@Column(name = "queued_at")
	private LocalDateTime queuedAt;

	@Column(name = "started_at")
	private LocalDateTime startedAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Column(name = "failure_detail", columnDefinition = "TEXT")
	private String failureDetail;

	/**
	 * 발송 전 DRAFT 캠페인을 생성한다.
	 * @param subject 정제된 제목
	 * @param sanitizedHtml 정제된 HTML 본문
	 * @param fromAddress 발신 주소
	 * @param replyToAddress 회신 주소
	 * @param filterJson 생성 당시 대상 필터 JSON
	 * @param recipientCount 수신자 수
	 * @param createdByUserId 생성 관리자 ID
	 * @return 초기 집계가 설정된 캠페인
	 */
	public static EmailCampaign of(
		String subject,
		String sanitizedHtml,
		String fromAddress,
		String replyToAddress,
		String filterJson,
		long recipientCount,
		String createdByUserId) {
		return EmailCampaign.builder()
			.subject(subject)
			.sanitizedHtml(sanitizedHtml)
			.fromAddress(fromAddress)
			.replyToAddress(replyToAddress)
			.filterJson(filterJson)
			.recipientCount(recipientCount)
			.pendingCount(recipientCount)
			.sentCount(0)
			.failedCount(0)
			.skippedCount(0)
			.status(EmailCampaignStatus.DRAFT)
			.createdByUserId(createdByUserId)
			.build();
	}

	/**
	 * DRAFT 캠페인을 QUEUED로 전환하고 발송 요청자를 기록한다.
	 * @param requestedByUserId 발송 요청 관리자 ID
	 * @param requestedAt 발송 요청 시각
	 */
	public void queue(String requestedByUserId, LocalDateTime requestedAt) {
		if (status != EmailCampaignStatus.DRAFT) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_SEND_ALREADY_REQUESTED.toBaseException();
		}
		status = EmailCampaignStatus.QUEUED;
		sendRequestedByUserId = requestedByUserId;
		sendRequestedAt = requestedAt;
		queuedAt = requestedAt;
	}

	/**
	 * QUEUED 캠페인을 SENDING으로 전환한다.
	 * @param now 발송 시작 시각
	 */
	public void start(LocalDateTime now) {
		validateStatus(EmailCampaignStatus.QUEUED);
		status = EmailCampaignStatus.SENDING;
		startedAt = now;
	}

	/**
	 * 수신자 최종 집계에 따라 캠페인을 완료 상태로 전환한다.
	 * @param sentCount 성공 수
	 * @param failedCount 실패 수
	 * @param skippedCount 제외 수
	 * @param now 완료 시각
	 */
	public void complete(long sentCount, long failedCount, long skippedCount, LocalDateTime now) {
		validateStatus(EmailCampaignStatus.SENDING);
		this.sentCount = sentCount;
		this.failedCount = failedCount;
		this.skippedCount = skippedCount;
		this.pendingCount = Math.max(0, recipientCount - sentCount - failedCount - skippedCount);
		this.completedAt = now;
		if (sentCount == 0 && failedCount > 0) {
			status = EmailCampaignStatus.FAILED;
		} else if (failedCount > 0) {
			status = EmailCampaignStatus.PARTIALLY_FAILED;
		} else {
			status = EmailCampaignStatus.COMPLETED;
		}
	}

	/**
	 * 발송 중인 캠페인을 시스템 실패로 종료한다.
	 * @param detail 실패 상세
	 * @param now 실패 시각
	 */
	public void fail(String detail, LocalDateTime now) {
		if (status != EmailCampaignStatus.QUEUED && status != EmailCampaignStatus.SENDING) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_STATUS.toBaseException();
		}
		status = EmailCampaignStatus.FAILED;
		failureDetail = detail;
		completedAt = now;
	}

	private void validateStatus(EmailCampaignStatus expected) {
		if (status != expected) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_STATUS.toBaseException();
		}
	}
}
