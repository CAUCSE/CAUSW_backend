package net.causw.app.main.domain.notification.notification.api.v2.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.notification.notification.api.v2.dto.request.AdminPushNotificationRequest;
import net.causw.app.main.domain.notification.notification.service.AdminNotificationService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/notifications")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "Notification Admin v2", description = "관리자 알림 관리 API")
public class NotificationAdminController {

	private final AdminNotificationService adminNotificationService;

	@PostMapping("/push")
	@Operation(summary = "특정 유저에게 FCM 푸시 알림 전송", description = "관리자가 특정 유저에게 FCM 푸시 알림을 전송합니다. "
		+ "saveNotification이 true이면 해당 유저의 알림함에도 저장됩니다.")
	public ApiResponse<Void> sendPushToUser(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody AdminPushNotificationRequest request) {

		adminNotificationService.sendPushToUser(
			userDetails.getUser(),
			request.userId(),
			request.title(),
			request.body(),
			request.saveNotification());

		return ApiResponse.success();
	}
}
