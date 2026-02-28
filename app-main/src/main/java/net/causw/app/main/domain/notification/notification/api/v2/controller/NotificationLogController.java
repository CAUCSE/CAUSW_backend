package net.causw.app.main.domain.notification.notification.api.v2.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationCountResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.service.NotificationLogService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Notification Public v2", description = "알림 로그 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notifications/log")
public class NotificationLogController {
	private final NotificationLogService notificationLogService;

	@GetMapping("/latest")
	@Operation(summary = "유저에게 온 최신 알람 조회(없을 시 null 반환)", description = "유저의 최신 알림을 조회합니다. <br>" +
		"해당 api는 홈 화면에서 읽지 않은 최신 알람 1개를 표시할 때 사용됩니다.")
	public ApiResponse<NotificationResponseDto> getNotificationTop1(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		NotificationResponseDto response = notificationLogService.getLatestUnread(userDetails.getUser().getId());

		return ApiResponse.success(response);
	}

	@GetMapping("/count")
	@Operation(summary = "유저에게 온 일반, 경조사 알림 중 읽지 않은 알림 총 개수 반환", description = "유저의 읽지 않은 알림 개수를 반환합니다.<br>" +
		"UI 상에서 10개 이상은 9+로 표기되기 때문에 10개까지 카운팅 되도록 하였습니다.")
	public ApiResponse<NotificationCountResponseDto> getNotificationLogCount(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		NotificationCountResponseDto response = notificationLogService
			.getNotificationLogCount(userDetails.getUser().getId());

		return ApiResponse.success(response);
	}

}
