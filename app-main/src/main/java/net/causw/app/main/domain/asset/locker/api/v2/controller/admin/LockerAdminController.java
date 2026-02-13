package net.causw.app.main.domain.asset.locker.api.v2.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request.LockerListRequest;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response.LockerListItemResponse;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.mapper.LockerListMapper;
import net.causw.app.main.domain.asset.locker.service.v2.LockerAdminService;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/lockers")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "관리자 사물함 API V2", description = "관리자 사물함 관리 API V2")
public class LockerAdminController {

	private final LockerAdminService lockerAdminService;
	private final LockerListMapper lockerListMapper;

	@GetMapping
	@Operation(summary = "사물함 목록 조회", description = "관리자용 사물함 목록을 페이지네이션으로 조회합니다.")
	public ApiResponse<Page<LockerListItemResponse>> getLockers(
		@ModelAttribute @Validated LockerListRequest request) {

		int page = request.page() != null ? request.page() : 0;
		int size = request.size() != null ? request.size() : 10;

		PageRequest pageRequest = PageRequest.of(page, size);

		Page<LockerListItemResponse> response = lockerAdminService
			.getLockerList(lockerListMapper.toCondition(request), pageRequest)
			.map(lockerListMapper::toResponse);

		return ApiResponse.success(response);
	}

	@GetMapping("/schedules/current")
	public ApiResponse<Void> getCurrentLockerSchedule() {

		if (true) {
			throw AuthErrorCode.INVALID_TOKEN.toBaseException();
		}
		return ApiResponse.success();
	}

}
