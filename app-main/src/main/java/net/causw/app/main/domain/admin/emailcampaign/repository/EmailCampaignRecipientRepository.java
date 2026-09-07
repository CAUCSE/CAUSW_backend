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

	long countByCampaignIdAndStatus(String campaignId, EmailCampaignRecipientStatus status);
}
