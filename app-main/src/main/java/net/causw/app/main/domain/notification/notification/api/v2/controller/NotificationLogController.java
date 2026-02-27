package net.causw.app.main.domain.notification.notification.api.v2.controller;

import static net.causw.global.constant.StaticValue.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationCountResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.service.NotificationLogService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Notification v2", description = "알림 로그 api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notifications/log")
public class NotificationLogController {
	private final NotificationLogService notificationLogService;

	@GetMapping
	@Operation(summary = "유저의 알림 리스트 조회", description = "유저의 알림 리스트를 조회합니다. <br>" +
		"오프셋 페이지네이션 기반이고 기본적으로 한 페이지에 20개의 알림입니다.")
	public ApiResponse<PageResponse<NotificationResponseDto>> getNotificationList(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(name = "pageNum", required = false, defaultValue = "0") Integer pageNum) {

		PageRequest pageRequest = PageRequest.of(pageNum, DEFAULT_PAGE_SIZE);

		PageResponse<NotificationResponseDto> response = PageResponse
			.from(notificationLogService.getNotificationList(userDetails.getUser().getId(), pageRequest));

		return ApiResponse.success(response);
	}

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
		"최대 10개까지 카운팅합니다.")
	public ApiResponse<NotificationCountResponseDto> getNotificationLogCount(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		NotificationCountResponseDto response = notificationLogService
			.getNotificationLogCount(userDetails.getUser().getId());

		return ApiResponse.success(response);
	}
}
