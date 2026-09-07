package net.causw.app.main.domain.admin.emailcampaign.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaignRecipient;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;

@Repository
public interface EmailCampaignRecipientRepository extends JpaRepository<EmailCampaignRecipient, Long> {

	/**
	 * 현재 시각에 발송 가능한 수신자 ID를 안정적인 순서로 조회한다.
	 * @param campaignId 캠페인 ID
	 * @param status 조회할 수신자 상태
	 * @param now 발송 가능 시각 판단 기준
	 * @param pageable 최대 조회 수
	 * @return 발송 가능한 수신자 ID 목록
	 */
	@Query("""
		select recipient.id
		from EmailCampaignRecipient recipient
		where recipient.campaign.id = :campaignId
		  and recipient.status = :status
		  and (recipient.nextAttemptAt is null or recipient.nextAttemptAt <= :now)
		order by recipient.id
		""")
	List<Long> findDispatchableIds(
		@Param("campaignId") String campaignId,
		@Param("status") EmailCampaignRecipientStatus status,
		@Param("now") LocalDateTime now,
		Pageable pageable);

	/**
	 * 수신자 상태와 재시도 시각을 조건으로 원자적으로 발송 권한을 claim한다.
	 * @param recipientId 수신자 ID
	 * @param pendingStatus claim 전 상태
	 * @param sendingStatus claim 후 상태
	 * @param now claim 시각
	 * @return 갱신된 행 수. 1이면 claim 성공이다.
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update EmailCampaignRecipient recipient
		set recipient.status = :sendingStatus,
		    recipient.attemptCount = recipient.attemptCount + 1,
		    recipient.lastAttemptAt = :now,
		    recipient.claimedAt = :now,
		    recipient.nextAttemptAt = null
		where recipient.id = :recipientId
		  and recipient.status = :pendingStatus
		  and (recipient.nextAttemptAt is null or recipient.nextAttemptAt <= :now)
		""")
	int claim(
		@Param("recipientId") Long recipientId,
		@Param("pendingStatus") EmailCampaignRecipientStatus pendingStatus,
		@Param("sendingStatus") EmailCampaignRecipientStatus sendingStatus,
		@Param("now") LocalDateTime now);

	/**
	 * timeout 기준보다 오래된 발송 claim을 재처리 가능한 상태로 일괄 복구한다.
	 * @param sendingStatus 복구 대상 상태
	 * @param pendingStatus 복구 후 상태
	 * @param claimedBefore stale claim 기준 시각
	 * @param nextAttemptAt 다음 시도 가능 시각
	 * @return 복구된 수신자 수
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update EmailCampaignRecipient recipient
		set recipient.status = :pendingStatus,
		    recipient.claimedAt = null,
		    recipient.nextAttemptAt = :nextAttemptAt
		where recipient.status = :sendingStatus
		  and recipient.claimedAt < :claimedBefore
		""")
	int releaseStaleClaims(
		@Param("sendingStatus") EmailCampaignRecipientStatus sendingStatus,
		@Param("pendingStatus") EmailCampaignRecipientStatus pendingStatus,
		@Param("claimedBefore") LocalDateTime claimedBefore,
		@Param("nextAttemptAt") LocalDateTime nextAttemptAt);

	/**
	 * 캠페인과 처리 상태에 해당하는 수신자 수를 계산한다.
	 * @param campaignId 캠페인 ID
	 * @param status 수신자 상태
	 * @return 조건에 해당하는 수신자 수
	 */
	long countByCampaignIdAndStatus(String campaignId, EmailCampaignRecipientStatus status);
}
