package net.causw.app.main.domain.community.systemnotice.api.v2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.systemnotice.api.v2.dto.response.SystemNoticeResponse;
import net.causw.app.main.domain.community.systemnotice.service.SystemNoticeService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/system-notices")
public class SystemNoticeController {

	private final SystemNoticeService systemNoticeService;

	@GetMapping("/latest")
	public ResponseEntity<ApiResponse<SystemNoticeResponse>> getLatest(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return systemNoticeService.getLatest(userDetails.getUser())
			.map(SystemNoticeResponse::from)
			.map(response -> ResponseEntity.ok(ApiResponse.success(response)))
			.orElseGet(() -> ResponseEntity.ok(ApiResponse.success()));
	}

	@PostMapping("/{postId}/read")
	public ApiResponse<Void> markAsRead(
		@PathVariable String postId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		systemNoticeService.markAsRead(userDetails.getUser(), postId);
		return ApiResponse.success();
	}
}
