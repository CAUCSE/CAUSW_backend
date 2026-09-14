package net.causw.app.main.domain.admin.emailcampaign.api.v2.mapper;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignCreateRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignFilterRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignListRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignRecipientListRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignSendRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response.EmailCampaignRecipientListItemResponse;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response.EmailCampaignResponse;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response.EmailCampaignTargetPreviewResponse;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignCreateCommand;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignFilter;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignListQuery;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignRecipientListQuery;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignRecipientListResult;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignResult;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignSendCommand;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignTargetPreviewResult;
import net.causw.app.main.domain.user.account.entity.user.User;

@Component
public class EmailCampaignApiMapper {

	/**
	 * API 필터 요청을 서비스 필터로 변환한다.
	 * @param request API 필터 요청
	 * @return 정규화되는 서비스 필터
	 */
	public EmailCampaignFilter toFilter(EmailCampaignFilterRequest request) {
		return new EmailCampaignFilter(request.admissionYears(), request.departments(), request.academicStatuses());
	}

	/**
	 * 캠페인 생성 요청과 인증 관리자를 생성 명령으로 변환한다.
	 * @param request 캠페인 생성 요청
	 * @param creator 생성 관리자
	 * @return 캠페인 생성 명령
	 */
	public EmailCampaignCreateCommand toCommand(EmailCampaignCreateRequest request, User creator) {
		return new EmailCampaignCreateCommand(
			request.subject(), request.html(), toFilter(request.filter()), creator);
	}

	/**
	 * 캠페인 발송 요청을 서비스 명령으로 변환한다.
	 * @param campaignId 캠페인 ID
	 * @param request 발송 확인 요청
	 * @param requester 발송 요청 관리자
	 * @return 캠페인 발송 명령
	 */
	public EmailCampaignSendCommand toCommand(
		String campaignId, EmailCampaignSendRequest request, User requester) {
		return new EmailCampaignSendCommand(
			campaignId, request.confirmedSubject(), request.confirmedRecipientCount(), requester);
	}

	/**
	 * 캠페인 목록 요청을 조회 조건으로 변환한다.
	 * @param request 캠페인 목록 요청
	 * @return 캠페인 조회 조건
	 */
	public EmailCampaignListQuery toQuery(EmailCampaignListRequest request) {
		return new EmailCampaignListQuery(request.status(), request.from(), request.to());
	}

	/**
	 * 수신자 목록 요청을 조회 조건으로 변환한다.
	 * @param campaignId 캠페인 ID
	 * @param request 수신자 상태 요청
	 * @return 수신자 조회 조건
	 */
	public EmailCampaignRecipientListQuery toQuery(String campaignId, EmailCampaignRecipientListRequest request) {
		return new EmailCampaignRecipientListQuery(campaignId, request.status());
	}

	/**
	 * 대상 미리보기 서비스 결과를 API 응답으로 변환한다.
	 * @param result 대상 미리보기 결과
	 * @return 대상 미리보기 응답
	 */
	public EmailCampaignTargetPreviewResponse toResponse(EmailCampaignTargetPreviewResult result) {
		return new EmailCampaignTargetPreviewResponse(
			result.recipientCount(), result.admissionYearDistribution(), result.departmentDistribution(),
			result.academicStatusDistribution());
	}

	/**
	 * 캠페인 서비스 결과를 API 응답으로 변환한다.
	 * @param result 캠페인 결과
	 * @return 캠페인 응답
	 */
	public EmailCampaignResponse toResponse(EmailCampaignResult result) {
		return new EmailCampaignResponse(
			result.id(), result.subject(), result.sanitizedHtml(), result.filterJson(), result.status(),
			result.recipientCount(), result.pendingCount(), result.sentCount(), result.failedCount(),
			result.skippedCount(), result.createdAt(), result.completedAt());
	}

	/**
	 * 수신자 처리 결과를 API 응답으로 변환한다.
	 * @param result 수신자 처리 결과
	 * @return 수신자 목록 항목 응답
	 */
	public EmailCampaignRecipientListItemResponse toResponse(EmailCampaignRecipientListResult result) {
		return new EmailCampaignRecipientListItemResponse(
			result.id(), result.maskedEmail(), result.status(), result.attemptCount(), result.lastAttemptAt(),
			result.sentAt(), result.lastErrorCode());
	}
}
