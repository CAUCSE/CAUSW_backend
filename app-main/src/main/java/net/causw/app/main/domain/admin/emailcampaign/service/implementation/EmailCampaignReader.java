package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaign;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;
import net.causw.app.main.domain.admin.emailcampaign.repository.EmailCampaignRepository;
import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignQueryRepository;
import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignRecipientItem;
import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignSummary;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailCampaignReader {

	private final EmailCampaignRepository campaignRepository;
	private final EmailCampaignQueryRepository campaignQueryRepository;

	/**
	 * 캠페인 ID로 엔티티를 조회한다.
	 * @param campaignId 캠페인 ID
	 * @return 조회된 캠페인
	 */
	public EmailCampaign getById(String campaignId) {
		return campaignRepository.findById(campaignId)
			.orElseThrow(EmailCampaignErrorCode.EMAIL_CAMPAIGN_NOT_FOUND::toBaseException);
	}

	/**
	 * 캠페인 요약 목록 조회를 QueryDSL repository에 위임한다.
	 * @param status 캠페인 상태
	 * @param from 생성 시각 하한
	 * @param to 생성 시각 상한
	 * @param pageable 페이지 정보
	 * @return 캠페인 요약 페이지
	 */
	public Page<EmailCampaignSummary> findCampaigns(
		EmailCampaignStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
		return campaignQueryRepository.findCampaigns(status, from, to, pageable);
	}

	/**
	 * 캠페인 존재 여부를 확인한 뒤 수신자 처리 결과를 페이지 조회한다.
	 * @param campaignId 캠페인 ID
	 * @param status 수신자 상태
	 * @param pageable 페이지 정보
	 * @return 수신자 처리 결과 페이지
	 */
	public Page<EmailCampaignRecipientItem> findRecipients(
		String campaignId, EmailCampaignRecipientStatus status, Pageable pageable) {
		getById(campaignId);
		return campaignQueryRepository.findRecipients(campaignId, status, pageable);
	}
}
