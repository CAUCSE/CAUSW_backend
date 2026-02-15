package net.causw.app.main.domain.asset.locker.api.v2.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@PostMapping("/{id}/register")
	@Operation(summary = "사물함 신청", description = "사물함을 신청합니다. 기존 사물함 보유 시 자동 반납됩니다.")
	public ApiResponse<Void> registerLocker(
		@PathVariable String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		lockerService.registerLocker(id, userDetails.getUser());
		return ApiResponse.success();
	}

	@PostMapping("/{id}/return")
	@Operation(summary = "사물함 반납", description = "사용중인 사물함을 반납합니다.")
	public ApiResponse<Void> returnLocker(
		@PathVariable String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		lockerService.returnLocker(id, userDetails.getUser());
		return ApiResponse.success();
	}

	@PostMapping("/{id}/extend")
	@Operation(summary = "사물함 연장", description = "사용중인 사물함의 만료일을 연장합니다.")
	public ApiResponse<Void> extendLocker(
		@PathVariable String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		lockerService.extendLocker(id, userDetails.getUser());
		return ApiResponse.success();
	}
}
