package net.causw.app.main.domain.user.academic.api.v2.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import net.causw.app.main.domain.user.academic.api.v2.dto.request.AcademicRecordApplicationListRequest;
import net.causw.app.main.domain.user.academic.api.v2.dto.request.AcademicRecordApplicationRejectRequest;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.AcademicRecordApplicationDetailResponse;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.AcademicRecordApplicationSummaryResponse;
import net.causw.app.main.domain.user.academic.api.v2.mapper.AcademicRecordApplicationDetailMapper;
import net.causw.app.main.domain.user.academic.api.v2.mapper.AcademicRecordApplicationListMapper;
import net.causw.app.main.domain.user.academic.service.AcademicRecordAdminService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/academic-records")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "관리자 학적상태 변경신청 관리 api", description = "관리자 학적상태 변경신청 관리 API")
public class AcademicRecordAdminController {

	private final AcademicRecordAdminService academicRecordAdminService;
	private final AcademicRecordApplicationListMapper applicationListMapper;
	private final AcademicRecordApplicationDetailMapper applicationDetailMapper;

	@Operation(summary = "학적 변경 신청 목록 조회", description = "학적 변경 신청(졸업 → 재학) 목록을 조회합니다. 신청 상태, 학과, 검색 키워드로 필터링할 수 있습니다.")
	@GetMapping("/applications")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Page<AcademicRecordApplicationSummaryResponse>> getApplications(
		@ParameterObject AcademicRecordApplicationListRequest request) {
		return ApiResponse.success(
			academicRecordAdminService.getApplications(applicationListMapper.toCondition(request))
				.map(applicationListMapper::toResponse));
	}

	@Operation(summary = "학적 변경 신청 상세 조회", description = "학적 변경 신청(졸업 → 재학)의 상세 정보를 조회합니다.")
	@GetMapping("/applications/{applicationId}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<AcademicRecordApplicationDetailResponse> getApplicationDetail(
		@Parameter(description = "신청서 ID") @PathVariable String applicationId) {
		return ApiResponse.success(
			applicationDetailMapper.toResponse(
				academicRecordAdminService.getApplicationDetail(applicationId)));
	}

	@Operation(summary = "학적 변경 신청 승인", description = "학적 변경 신청(졸업 → 재학)을 승인합니다. 대상 사용자의 학적 상태가 변경되고 처리 로그가 기록됩니다.")
	@PostMapping("/applications/{applicationId}/approve")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> approveApplication(
		@AuthenticationPrincipal CustomUserDetails adminDetails,
		@Parameter(description = "신청서 ID") @PathVariable String applicationId) {
		academicRecordAdminService.approve(
			adminDetails.getUser(),
			applicationId);
		return ApiResponse.success();
	}

	@Operation(summary = "학적 변경 신청 반려", description = "학적 변경 신청(졸업 → 재학)을 반려합니다. 반려 사유가 기록되고 처리 로그가 생성됩니다.")
	@PostMapping("/applications/{applicationId}/reject")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> rejectApplication(
		@AuthenticationPrincipal CustomUserDetails adminDetails,
		@Parameter(description = "신청서 ID") @PathVariable String applicationId,
		@RequestBody @Valid AcademicRecordApplicationRejectRequest requestDto) {
		academicRecordAdminService.reject(
			adminDetails.getUser(),
			applicationId,
			requestDto.rejectReason());
		return ApiResponse.success();
	}
}
