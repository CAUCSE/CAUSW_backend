package net.causw.app.main.domain.user.account.api.v2.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import net.causw.app.main.domain.user.account.api.v2.dto.request.AdmissionListRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.AdmissionRejectRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserDropRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserListRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserRoleUpdateRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.response.AdmissionListItemResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.AdmissionResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserDetailResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserListItemResponse;
import net.causw.app.main.domain.user.account.api.v2.mapper.AdmissionDtoMapper;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserDetailMapper;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserListMapper;
import net.causw.app.main.domain.user.account.service.AdmissionAdminService;
import net.causw.app.main.domain.user.account.service.UserAdminService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/users")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "User Admin v2", description = "관리자 권한으로 사용자 계정을 관리하는 API")
public class UserAdminController {

	private final UserAdminService userAdminService;
	private final AdmissionAdminService admissionAdminService;
	private final UserListMapper userListMapper;
	private final UserDetailMapper userDetailMapper;
	private final AdmissionDtoMapper admissionDtoMapper;

	// ── 회원 관리 ──

	@Operation(summary = "회원 목록 조회 V2", description = "관리자가 회원 목록을 조회합니다. 이름/학번 키워드 검색, 상태/학적/학과 필터링, 페이징을 지원합니다.")
	@GetMapping
	public ApiResponse<PageResponse<UserListItemResponse>> getUsers(
		@ModelAttribute @Validated UserListRequest request,
		@PageableDefault(page = 0, size = 10) Pageable pageable) {

		PageResponse<UserListItemResponse> response = PageResponse.from(
			userAdminService
				.getUserList(userListMapper.toCondition(request), pageable)
				.map(userListMapper::toResponse));

		return ApiResponse.success(response);
	}

	@Operation(summary = "회원 상세 조회 V2", description = "관리자가 특정 회원의 상세 정보를 조회합니다.")
	@GetMapping("/{userId}")
	public ApiResponse<UserDetailResponse> getUserDetail(
		@PathVariable String userId) {

		var userDetail = userAdminService.getUserDetail(userId);
		return ApiResponse.success(userDetailMapper.toResponse(userDetail));
	}

	@Operation(summary = "회원 추방 V2", description = "관리자가 사용자를 추방합니다. 추방 시 사용자 상태가 DROP으로 변경됩니다.")
	@PatchMapping("/{userId}/drop")
	public ApiResponse<Void> dropUser(
		@PathVariable String userId,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid UserDropRequest request) {

		userAdminService.dropUser(userDetails.getUser(), userId, request.dropReason());
		return ApiResponse.success();
	}

	@Operation(summary = "회원 복구 V2", description = "관리자가 추방된 사용자를 복구합니다. 복구 시 사용자 상태가 ACTIVE로 변경됩니다.")
	@PatchMapping("/{userId}/restore")
	public ApiResponse<Void> restoreUser(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable String userId) {

		userAdminService.restoreUser(userDetails.getUser(), userId);
		return ApiResponse.success();
	}

	@Operation(summary = "회원 권한 변경 V2", description = "관리자가 회원의 현재 권한을 확인한 뒤 지정한 권한으로 변경합니다.")
	@PatchMapping("/{userId}/role")
	public ApiResponse<Void> replaceUserRole(
		@PathVariable String userId,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid UserRoleUpdateRequest request) {

		userAdminService.replaceUserRole(userDetails.getUser(), userId, request.currentRole(), request.newRole());
		return ApiResponse.success();
	}

	// ── 재학정보 인증 ──

	@Operation(summary = "재학인증 신청 목록 조회 V2", description = "관리자가 재학인증 신청 목록을 조회합니다. "
		+ "이름/학번 키워드 검색, 사용자 상태(AWAIT/REJECT) 필터링, 페이징을 지원합니다.")
	@GetMapping("/admissions")
	public ApiResponse<PageResponse<AdmissionListItemResponse>> getAdmissions(
		@ModelAttribute @Validated AdmissionListRequest request,
		@PageableDefault(page = 0, size = 10) Pageable pageable) {

		PageResponse<AdmissionListItemResponse> response = PageResponse.from(
			admissionAdminService
				.getAdmissionList(admissionDtoMapper.toCondition(request), pageable)
				.map(admissionDtoMapper::toListItemResponse));

		return ApiResponse.success(response);
	}

	@Operation(summary = "재학인증 신청 상세 조회 V2", description = "관리자가 특정 재학인증 신청의 상세 정보를 조회합니다. "
		+ "신청자 정보, 증빙서류 이미지 URL, 재학 분류 등 전체 정보를 확인할 수 있습니다.")
	@GetMapping("/admissions/{admissionId}")
	public ApiResponse<AdmissionResponse> getAdmissionDetail(
		@PathVariable String admissionId) {

		var result = admissionAdminService.getAdmissionDetail(admissionId);
		return ApiResponse.success(admissionDtoMapper.toResponse(result));
	}

	@Operation(summary = "재학인증 신청 승인 V2", description = "관리자가 재학인증 신청을 승인합니다. "
		+ "승인 시 신청서에 기재된 학적 정보로 사용자 정보가 업데이트되고, "
		+ "사용자 상태가 ACTIVE로 변경됩니다.")
	@PostMapping("/admissions/{admissionId}/approve")
	public ApiResponse<Void> approveAdmission(
		@PathVariable String admissionId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		admissionAdminService.approveAdmission(admissionId, userDetails.getUser());
		return ApiResponse.success();
	}

	@Operation(summary = "재학인증 신청 거절 V2", description = "관리자가 재학인증 신청을 거절합니다. "
		+ "거절 시 거절 사유가 기록되고, 사용자 상태가 REJECT로 변경됩니다.")
	@PostMapping("/admissions/{admissionId}/reject")
	public ApiResponse<Void> rejectAdmission(
		@PathVariable String admissionId,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid AdmissionRejectRequest request) {

		admissionAdminService.rejectAdmission(admissionId, userDetails.getUser(), request.rejectReason());
		return ApiResponse.success();
	}
}
