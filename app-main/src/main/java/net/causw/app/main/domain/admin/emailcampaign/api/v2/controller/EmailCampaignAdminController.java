package net.causw.app.main.domain.admin.emailcampaign.api.v2.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.core.aop.annotation.RequireAdminRole;
import net.causw.app.main.core.aop.enums.AdminTarget;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignCreateRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignListRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignRecipientListRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignSendRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignTargetPreviewRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response.EmailCampaignRecipientListItemResponse;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response.EmailCampaignResponse;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response.EmailCampaignTargetPreviewResponse;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.mapper.EmailCampaignApiMapper;
import net.causw.app.main.domain.admin.emailcampaign.service.EmailCampaignService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/email-campaigns")
@PreAuthorize("@security.hasAnyRole(@Role.ADMIN, @Role.SYSTEM_ADMIN)")
@RequireAdminRole(target = AdminTarget.ALL_ADMIN)
@Tag(name = "Admin Email Campaign v2", description = "관리자 이메일 캠페인 API")
public class EmailCampaignAdminController {

	private final EmailCampaignService campaignService;
	private final EmailCampaignApiMapper campaignApiMapper;

	@PostMapping("/target-preview")
	@Operation(summary = "이메일 캠페인 대상 미리보기")
	public ApiResponse<EmailCampaignTargetPreviewResponse> previewTargets(
		@Valid @RequestBody EmailCampaignTargetPreviewRequest request) {
		return ApiResponse.success(campaignApiMapper.toResponse(
			campaignService.previewTargets(campaignApiMapper.toFilter(request.filter()))));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "이메일 캠페인 생성")
	public ApiResponse<EmailCampaignResponse> createCampaign(
		@Valid @RequestBody EmailCampaignCreateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(campaignApiMapper.toResponse(
			campaignService.createCampaign(campaignApiMapper.toCommand(request, userDetails.getUser()))));
	}

	@PostMapping("/{campaignId}/send")
	@Operation(summary = "이메일 캠페인 발송 요청")
	public ApiResponse<EmailCampaignResponse> requestSend(
		@PathVariable String campaignId,
		@Valid @RequestBody EmailCampaignSendRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(campaignApiMapper.toResponse(
			campaignService.requestSend(
				campaignApiMapper.toCommand(campaignId, request, userDetails.getUser()))));
	}

	@GetMapping
	@Operation(summary = "이메일 캠페인 목록 조회")
	public ApiResponse<PageResponse<EmailCampaignResponse>> getCampaigns(
		@ModelAttribute EmailCampaignListRequest request,
		@PageableDefault(page = 0, size = 10) Pageable pageable) {
		return ApiResponse.success(PageResponse.from(
			campaignService.getCampaigns(campaignApiMapper.toQuery(request), pageable)
				.map(campaignApiMapper::toResponse)));
	}

	@GetMapping("/{campaignId}")
	@Operation(summary = "이메일 캠페인 상세 조회")
	public ApiResponse<EmailCampaignResponse> getCampaignDetail(@PathVariable String campaignId) {
		return ApiResponse.success(campaignApiMapper.toResponse(campaignService.getCampaignDetail(campaignId)));
	}

	@GetMapping("/{campaignId}/recipients")
	@Operation(summary = "이메일 캠페인 수신자 목록 조회")
	public ApiResponse<PageResponse<EmailCampaignRecipientListItemResponse>> getCampaignRecipients(
		@PathVariable String campaignId,
		@ModelAttribute EmailCampaignRecipientListRequest request,
		@PageableDefault(page = 0, size = 20) Pageable pageable) {
		return ApiResponse.success(PageResponse.from(
			campaignService.getCampaignRecipients(campaignApiMapper.toQuery(campaignId, request), pageable)
				.map(campaignApiMapper::toResponse)));
	}
}
