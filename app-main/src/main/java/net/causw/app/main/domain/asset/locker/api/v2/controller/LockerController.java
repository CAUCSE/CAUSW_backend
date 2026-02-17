package net.causw.app.main.domain.asset.locker.api.v2.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.asset.locker.api.v2.controller.dto.response.LockerFloorListResponse;
import net.causw.app.main.domain.asset.locker.api.v2.controller.dto.response.LockerLocationResponse;
import net.causw.app.main.domain.asset.locker.api.v2.controller.dto.response.LockerPeriodStatusResponse;
import net.causw.app.main.domain.asset.locker.api.v2.controller.dto.response.MyLockerResponse;
import net.causw.app.main.domain.asset.locker.api.v2.mapper.LockerResponseMapper;
import net.causw.app.main.domain.asset.locker.service.v2.LockerService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/lockers")
@Tag(name = "사물함 API V2", description = "사물함 API V2")
public class LockerController {

	private final LockerService lockerService;
	private final LockerResponseMapper lockerResponseMapper;

	@GetMapping("/period-status")
	@Operation(summary = "현재 기간 정책 조회", description = "현재 사물함 기간 정책 상태(READY/APPLY/EXTEND/CLOSED)를 조회합니다.")
	public ApiResponse<LockerPeriodStatusResponse> findCurrentPeriodStatus() {

		return ApiResponse.success(
			lockerResponseMapper.toPeriodStatusResponse(lockerService.findCurrentPeriodStatus()));
	}

	@GetMapping("/locations")
	@Operation(summary = "전체 층 리스트 조회", description = "전체 사물함 층별 요약 정보를 조회합니다.")
	public ApiResponse<LockerFloorListResponse> findAllFloors() {

		return ApiResponse.success(
			lockerResponseMapper.toFloorListResponse(lockerService.findAllFloors()));
	}

	@GetMapping("/me")
	@Operation(summary = "내 사물함 조회", description = "현재 로그인한 유저의 사물함 정보를 조회합니다.")
	public ApiResponse<MyLockerResponse> findMyLocker(
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ApiResponse.success(
			lockerResponseMapper.toMyLockerResponse(lockerService.findMyLocker(userDetails.getUserId())));
	}

	@GetMapping("/locations/{locationId}")
	@Operation(summary = "층별 사물함 조회", description = "특정 층의 사물함 목록과 정책/액션 정보를 조회합니다.")
	public ApiResponse<LockerLocationResponse> findByLocation(
		@PathVariable String locationId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ApiResponse.success(
			lockerResponseMapper.toLocationResponse(lockerService.findByLocation(locationId, userDetails.getUserId())));
	}

	@PostMapping("/{id}/register")
	@Operation(summary = "사물함 신청", description = "사물함을 신청합니다. 기존 사물함 보유 시 자동 반납됩니다.")
	public ApiResponse<Void> registerLocker(
		@PathVariable String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		lockerService.registerLocker(id, userDetails.getUserId());
		return ApiResponse.success();
	}

	@PostMapping("/{id}/return")
	@Operation(summary = "사물함 반납", description = "사용중인 사물함을 반납합니다.")
	public ApiResponse<Void> returnLocker(
		@PathVariable String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		lockerService.returnLocker(id, userDetails.getUserId());
		return ApiResponse.success();
	}

	@PostMapping("/{id}/extend")
	@Operation(summary = "사물함 연장", description = "사용중인 사물함의 만료일을 연장합니다.")
	public ApiResponse<Void> extendLocker(
		@PathVariable String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		lockerService.extendLocker(id, userDetails.getUserId());
		return ApiResponse.success();
	}
}
