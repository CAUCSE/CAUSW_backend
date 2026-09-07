package net.causw.app.main.domain.admin.emailcampaign.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaign;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;

@Repository
public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, String> {

	List<EmailCampaign> findByStatusIn(Collection<EmailCampaignStatus> statuses);
}
