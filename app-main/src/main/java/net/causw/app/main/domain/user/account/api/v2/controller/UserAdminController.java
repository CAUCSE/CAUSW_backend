package net.causw.app.main.domain.user.account.api.v2.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import net.causw.app.main.domain.user.account.api.v2.dto.request.AdmissionListRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserListRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.response.AdmissionListItemResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.AdmissionResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserDetailResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserListItemResponse;
import net.causw.app.main.domain.user.account.api.v2.mapper.AdmissionDtoMapper;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserDetailMapper;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserListMapper;
import net.causw.app.main.domain.user.account.service.AdmissionAdminService;
import net.causw.app.main.domain.user.account.service.UserAdminService;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/users")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "User Admin v2", description = "관리자 회원/재학인증 관리 API")
public class UserAdminController {

	private final UserAdminService userAdminService;
	private final AdmissionAdminService admissionAdminService;
	private final UserListMapper userListMapper;
	private final UserDetailMapper userDetailMapper;
	private final AdmissionDtoMapper admissionDtoMapper;

	// ── 회원 관리 ──

	@Operation(summary = "회원 목록 조회 V2",
		description = "관리자가 회원 목록을 조회합니다. 이름/학번 키워드 검색, 상태/학적/학과 필터링, 페이징을 지원합니다.")
	@GetMapping
	public ApiResponse<Page<UserListItemResponse>> getUsers(
		@ModelAttribute @Validated UserListRequest request) {
		// page/size 안 보내면 기본값
		int page = request.page() != null ? request.page() : 0;
		int size = request.size() != null ? request.size() : 10;

		PageRequest pageRequest = PageRequest.of(page, size);

		Page<UserListItemResponse> response = userAdminService
			.getUserList(userListMapper.toCondition(request), pageRequest)
			.map(userListMapper::toResponse);

		return ApiResponse.success(response);
	}

	@Operation(summary = "회원 상세 조회 V2",
		description = "관리자가 특정 회원의 상세 정보를 조회합니다.")
	@GetMapping("/{userId}")
	public ApiResponse<UserDetailResponse> getUserDetail(
		@PathVariable String userId) {

		var userDetail = userAdminService.getUserDetail(userId);
		return ApiResponse.success(userDetailMapper.toResponse(userDetail));
	}

	// ── 재학정보 인증 ──

	@Operation(summary = "재학인증 신청 목록 조회 V2",
		description = "관리자가 재학인증 신청 목록을 조회합니다. "
			+ "이름/학번 키워드 검색, 사용자 상태(AWAIT/REJECT) 필터링, 페이징을 지원합니다.")
	@GetMapping("/admissions")
	public ApiResponse<Page<AdmissionListItemResponse>> getAdmissions(
		@ModelAttribute @Validated AdmissionListRequest request) {

		int page = request.page() != null ? request.page() : 0;
		int size = request.size() != null ? request.size() : 10;

		PageRequest pageRequest = PageRequest.of(page, size);

		Page<AdmissionListItemResponse> response = admissionAdminService
			.getAdmissionList(admissionDtoMapper.toCondition(request), pageRequest)
			.map(admissionDtoMapper::toListItemResponse);

		return ApiResponse.success(response);
	}

	@Operation(summary = "재학인증 신청 상세 조회 V2",
		description = "관리자가 특정 재학인증 신청의 상세 정보를 조회합니다. "
			+ "신청자 정보, 증빙서류 이미지 URL, 재학 분류 등 전체 정보를 확인할 수 있습니다.")
	@GetMapping("/admissions/{admissionId}")
	public ApiResponse<AdmissionResponse> getAdmissionDetail(
		@PathVariable String admissionId) {

		var result = admissionAdminService.getAdmissionDetail(admissionId);
		return ApiResponse.success(admissionDtoMapper.toResponse(result));
	}
}
