package net.causw.app.main.domain.admin.emailcampaign.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaign;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;

@Repository
public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, String> {

	/**
	 * 지정한 상태 중 하나에 해당하는 캠페인을 조회한다.
	 * @param statuses 조회할 캠페인 상태
	 * @return 상태 조건에 해당하는 캠페인 목록
	 */
	List<EmailCampaign> findByStatusIn(Collection<EmailCampaignStatus> statuses);
}
