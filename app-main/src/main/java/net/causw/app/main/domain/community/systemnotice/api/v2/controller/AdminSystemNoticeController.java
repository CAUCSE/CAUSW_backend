package net.causw.app.main.domain.community.systemnotice.api.v2.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.core.aop.annotation.RequireAdminRole;
import net.causw.app.main.core.aop.enums.AdminTarget;
import net.causw.app.main.domain.community.systemnotice.api.v2.dto.request.SystemNoticeCreateRequest;
import net.causw.app.main.domain.community.systemnotice.api.v2.dto.request.SystemNoticeUpdateRequest;
import net.causw.app.main.domain.community.systemnotice.api.v2.dto.response.SystemNoticeCreateResponse;
import net.causw.app.main.domain.community.systemnotice.api.v2.mapper.SystemNoticeDtoMapper;
import net.causw.app.main.domain.community.systemnotice.service.SystemNoticeService;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeCreateResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/system-notices")
@PreAuthorize("@security.hasAnyRole(@Role.ADMIN, @Role.SYSTEM_ADMIN)")
@RequireAdminRole(target = AdminTarget.SYSTEM_ONLY)
@Tag(name = "System Notice Admin v2", description = "관리자 시스템 공지 관리 API")
public class AdminSystemNoticeController {

	private final SystemNoticeService systemNoticeService;
	private final SystemNoticeDtoMapper systemNoticeDtoMapper;

	@GetMapping
	@Operation(summary = "시스템 공지 목록 조회", description = "시스템 공지를 최신순으로 조회합니다.")
	public ApiResponse<List<SystemNoticeCreateResponse>> getAll(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(systemNoticeDtoMapper.toCreateResponseList(systemNoticeService.getAll()));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "시스템 공지 작성", description = "새로운 시스템 공지를 작성합니다.")
	public ApiResponse<SystemNoticeCreateResponse> create(
		@Valid @RequestBody SystemNoticeCreateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		SystemNoticeCreateResult result = systemNoticeService.create(
			systemNoticeDtoMapper.toCreateCommand(request, userDetails.getUser()));
		return ApiResponse.success(systemNoticeDtoMapper.toCreateResponse(result));
	}

	@PutMapping("/{id}")
	@Operation(summary = "시스템 공지 수정", description = "기존 시스템 공지의 내용을 수정합니다.")
	public ApiResponse<Void> update(
		@PathVariable String id,
		@Valid @RequestBody SystemNoticeUpdateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		systemNoticeService.update(
			id, systemNoticeDtoMapper.toUpdateCommand(request, userDetails.getUser()));
		return ApiResponse.success();
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "시스템 공지 삭제", description = "기존 시스템 공지를 삭제합니다.")
	public ApiResponse<Void> delete(
		@PathVariable String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		systemNoticeService.delete(id, userDetails.getUser());
		return ApiResponse.success();
	}
}
