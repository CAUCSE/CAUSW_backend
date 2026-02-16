package net.causw.app.main.domain.asset.locker.api.v2.controller.admin;

import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request.LockerPolicyExtendStatusRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request.LockerPolicyExtendPeriodRequest;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request.LockerPolicyRegisterPeriodRequest;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request.LockerPolicyRegisterStatusRequest;
import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response.LockerPolicyResponse;
import net.causw.app.main.domain.asset.locker.service.v2.LockerPolicyAdminService;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/lockers/policy")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "관리자 사물함 정책 API V2", description = "관리자 사물함 정책 관리 API V2")
public class LockerPolicyAdminController {

	private final LockerPolicyAdminService lockerPolicyAdminService;

	@GetMapping
	@Operation(summary = "사물함 정책 조회", description = "현재 사물함 정책(만료일, 신청 기간, 연장 기간 등)을 조회합니다.")
	public ApiResponse<LockerPolicyResponse> getPolicy() {
		return ApiResponse.success(lockerPolicyAdminService.getPolicy());
	}

	@PutMapping("/register-period")
	@Operation(summary = "사물함 신청 기간 설정", description = "사물함 신청 시작일과 종료일을 설정합니다.")
	public ApiResponse<Void> updateRegisterPeriod(
		@RequestBody @Valid LockerPolicyRegisterPeriodRequest request) {

		lockerPolicyAdminService.updateRegisterPeriod(request.registerStartAt(), request.registerEndAt(),
			request.expiredAt());
		return ApiResponse.success();
	}

	@PutMapping("/register-status")
	@Operation(summary = "사물함 신청 상태 설정", description = "사물함 신청 가능 상태를 설정합니다.")
	public ApiResponse<Void> updateRegisterStatus(
		@RequestBody @Validated LockerPolicyRegisterStatusRequest request) {

		lockerPolicyAdminService.updateRegisterStatus(request.status());
		return ApiResponse.success();
	}

	@PutMapping("/extend-period")
	@Operation(summary = "사물함 연장 기간 설정", description = "사물함 연장 시작일, 종료일, 다음 만료일을 설정합니다.")
	public ApiResponse<Void> updateExtendPeriod(
		@RequestBody @Valid LockerPolicyExtendPeriodRequest request) {

		lockerPolicyAdminService.updateExtendPeriod(request.extendStartAt(), request.extendEndAt(),
			request.nextExpiredAt());
		return ApiResponse.success();
	}

	@PutMapping("/extend-status")
	@Operation(summary = "사물함 연장 상태 설정", description = "사물함 연장 가능 상태를 설정합니다.")
	public ApiResponse<Void> updateExtendStatus(
			@RequestBody @Validated LockerPolicyExtendStatusRequest request) {

		lockerPolicyAdminService.updateExtendStatus(request.status());
		return ApiResponse.success();
	}
}
