package net.causw.app.main.domain.community.systemnotice.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.systemnotice.api.v2.dto.request.SystemNoticeCreateRequest;
import net.causw.app.main.domain.community.systemnotice.api.v2.dto.request.SystemNoticeUpdateRequest;
import net.causw.app.main.domain.community.systemnotice.api.v2.dto.response.SystemNoticeCreateResponse;
import net.causw.app.main.domain.community.systemnotice.api.v2.mapper.SystemNoticeDtoMapper;
import net.causw.app.main.domain.community.systemnotice.service.SystemNoticeService;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeCreateResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/system-notices")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
public class AdminSystemNoticeController {

	private final SystemNoticeService systemNoticeService;
	private final SystemNoticeDtoMapper systemNoticeDtoMapper;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<SystemNoticeCreateResponse> create(
		@Valid @RequestBody SystemNoticeCreateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		SystemNoticeCreateResult result = systemNoticeService.create(
			systemNoticeDtoMapper.toCreateCommand(request, userDetails.getUser()));
		return ApiResponse.success(systemNoticeDtoMapper.toCreateResponse(result));
	}

	@PutMapping("/{id}")
	public ApiResponse<Void> update(
		@PathVariable String id,
		@Valid @RequestBody SystemNoticeUpdateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		systemNoticeService.update(
			id, systemNoticeDtoMapper.toUpdateCommand(request, userDetails.getUser()));
		return ApiResponse.success();
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> delete(
		@PathVariable String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		systemNoticeService.delete(id, userDetails.getUser());
		return ApiResponse.success();
	}
}
