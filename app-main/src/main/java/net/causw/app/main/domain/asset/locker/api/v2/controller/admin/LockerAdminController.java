package net.causw.app.main.domain.asset.locker.api.v2.controller.admin;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request.LockerAssignRequest;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request.LockerExtendRequest;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request.LockerListRequest;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request.LockerLogListRequest;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response.LockerListItemResponse;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response.LockerLogListItemResponse;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.mapper.LockerListMapper;
import net.causw.app.main.domain.asset.locker.service.v2.LockerAdminService;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/lockers")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "관리자 사물함 API V2", description = "관리자 사물함 관리 API V2")
public class LockerAdminController {

	private final LockerAdminService lockerAdminService;
	private final LockerListMapper lockerListMapper;

	@GetMapping("/logs")
	@Operation(summary = "사물함 로그 목록 조회", description = "관리자용 사물함 로그 목록을 페이지네이션으로 조회합니다.")
	public ApiResponse<PageResponse<LockerLogListItemResponse>> getLockerLogs(
		@ModelAttribute @Validated LockerLogListRequest request) {

		int page = request.page() != null ? request.page() : 0;
		int size = request.size() != null ? request.size() : 10;

		PageRequest pageRequest = PageRequest.of(page, size);

		PageResponse<LockerLogListItemResponse> response = PageResponse.from(
			lockerAdminService
				.getLockerLogList(lockerListMapper.toCondition(request), pageRequest)
				.map(lockerListMapper::toResponse));

		return ApiResponse.success(response);
	}

	@GetMapping
	@Operation(summary = "사물함 목록 조회", description = "관리자용 사물함 목록을 페이지네이션으로 조회합니다.")
	public ApiResponse<PageResponse<LockerListItemResponse>> getLockers(
		@ModelAttribute @Validated LockerListRequest request) {

		int page = request.page() != null ? request.page() : 0;
		int size = request.size() != null ? request.size() : 10;

		PageRequest pageRequest = PageRequest.of(page, size);

		PageResponse<LockerListItemResponse> response = PageResponse.from(
			lockerAdminService
				.getLockerList(lockerListMapper.toCondition(request), pageRequest)
				.map(lockerListMapper::toResponse));

		return ApiResponse.success(response);
	}

	@PostMapping("/{id}/assign")
	@Operation(summary = "사물함 배정", description = "비어 있는 사물함에 사용자를 배정합니다.")
	public ApiResponse<Void> assignLocker(
		@PathVariable String id,
		@RequestBody @Valid LockerAssignRequest request) {

		lockerAdminService.assignLocker(id, request.userId(), request.expiredAt());
		return ApiResponse.success();
	}

	@PostMapping("/{id}/extend")
	@Operation(summary = "사물함 만료일 연장", description = "사용중인 사물함의 만료일을 연장합니다.")
	public ApiResponse<Void> extendLocker(
		@PathVariable String id,
		@RequestBody @Valid LockerExtendRequest request) {

		lockerAdminService.extendLocker(id, request.expiredAt());
		return ApiResponse.success();
	}

	@PostMapping("/{id}/release")
	@Operation(summary = "사물함 회수", description = "사용중인 사물함을 회수합니다.")
	public ApiResponse<Void> releaseLocker(
		@PathVariable String id) {

		lockerAdminService.releaseLocker(id);
		return ApiResponse.success();
	}

	@PostMapping("/{id}/enable")
	@Operation(summary = "사물함 활성화", description = "비활성화된 사물함을 활성화합니다.")
	public ApiResponse<Void> enableLocker(
		@PathVariable String id) {

		lockerAdminService.enableLocker(id);
		return ApiResponse.success();
	}

	@PostMapping("/{id}/disable")
	@Operation(summary = "사물함 비활성화", description = "활성화된 사물함을 비활성화합니다. 사용자가 있는 경우 함께 해제됩니다.")
	public ApiResponse<Void> disableLocker(
		@PathVariable String id) {

		lockerAdminService.disableLocker(id);
		return ApiResponse.success();
	}
}
