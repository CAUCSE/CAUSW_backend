package net.causw.app.main.domain.community.ceremony.api.v2.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CeremonyAdminListRequest;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CeremonyRejectRequest;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyAdminListResponseDto;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyDetailResponseDto;
import net.causw.app.main.domain.community.ceremony.api.v2.mapper.CeremonyAdminListMapper;
import net.causw.app.main.domain.community.ceremony.api.v2.mapper.CeremonyDtoMapper;
import net.causw.app.main.domain.community.ceremony.service.CeremonyAdminService;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "관리자 경조사 API V2", description = "관리자 경조사 관리 API V2")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/ceremonies")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
public class CeremonyAdminController {

	private final CeremonyAdminService ceremonyAdminService;
	private final CeremonyAdminListMapper ceremonyAdminListMapper;
	private final CeremonyDtoMapper ceremonyDtoMapper;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "경조사 목록 조회", description = "경조사 목록을 조회합니다. 시작일, 종료일, 상태로 필터링할 수 있습니다.")
	public ApiResponse<PageResponse<CeremonyAdminListResponseDto>> getCeremonyList(
		@ParameterObject CeremonyAdminListRequest request,
		@ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
		return ApiResponse.success(
			PageResponse.from(
				ceremonyAdminService.getCeremonyList(ceremonyAdminListMapper.toCondition(request), pageable)
					.map(ceremonyDtoMapper::toAdminCeremonyListResponseDto)));
	}

	@GetMapping("/{ceremonyId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "경조사 상세 조회", description = "경조사 상세 정보를 조회합니다.")
	public ApiResponse<CeremonyDetailResponseDto> getCeremonyDetail(
		@Parameter(description = "경조사 ID") @PathVariable("ceremonyId") String ceremonyId) {
		return ApiResponse.success(
			ceremonyDtoMapper.toAdminCeremonyDetailResponseDto(
				ceremonyAdminService.getCeremonyDetail(ceremonyId)));
	}

	@PostMapping("/{ceremonyId}/approve")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "경조사 승인", description = "대기 중인 경조사를 승인합니다.")
	public ApiResponse<Void> approve(
		@Parameter(description = "경조사 ID") @PathVariable("ceremonyId") String ceremonyId) {
		ceremonyAdminService.approve(ceremonyId);
		return ApiResponse.success();
	}

	@PostMapping("/{ceremonyId}/reject")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "경조사 거절", description = "대기 중인 경조사를 거절합니다.")
	public ApiResponse<Void> reject(
		@Parameter(description = "경조사 ID") @PathVariable("ceremonyId") String ceremonyId,
		@RequestBody @Valid CeremonyRejectRequest request) {
		ceremonyAdminService.reject(ceremonyId, request.rejectReason());
		return ApiResponse.success();
	}
}
