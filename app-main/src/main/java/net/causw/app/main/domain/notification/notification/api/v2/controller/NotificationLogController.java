package net.causw.app.main.domain.notification.notification.api.v2.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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

	@GetMapping
	@Operation(summary = "유저의 알림 리스트 조회", description = "최근 7일의 알림을 리스트로 조회합니다. 알림의 isRead는 읽음 여부입니다. targetId는 게시글, 댓글 등의 id이고, targetParantId는 게시판의 id입니다.")
	public ApiResponse<List<NotificationResponseDto>> getNotificationList(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(name = "isRead") boolean isRead) {

		List<NotificationResponseDto> response = notificationLogService
			.getNotificationList(userDetails.getUser().getId(), isRead);

		return ApiResponse.success(response);
	}

	@GetMapping("/latest")
	@Operation(summary = "유저에게 온 최신 알림 조회(없을 시 null 반환)", description = "유저의 최신 알림을 조회합니다. 해당 api는 홈 화면에서 읽지 않은 최신 알림 1개를 표시할 때 사용됩니다.")
	public ApiResponse<NotificationResponseDto> getNotificationTop1(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		NotificationResponseDto response = notificationLogService.getLatestUnread(userDetails.getUser().getId());

		return ApiResponse.success(response);
	}

	@GetMapping("/count")
	@Operation(summary = "유저에게 온 일반, 경조사 알림 중 읽지 않은 알림 총 개수 반환", description = "유저의 읽지 않은 알림 개수를 반환합니다. 최대 10개까지 카운팅합니다.")
	public ApiResponse<NotificationCountResponseDto> getNotificationLogCount(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		NotificationCountResponseDto response = notificationLogService
			.getNotificationLogCount(userDetails.getUser().getId());

		return ApiResponse.success(response);
	}

	@PatchMapping("/{id}/read")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "유저에게 온 알람 읽음으로 변경", description = "유저의 알람 조회 여부를 참으로 변경합니다. id에는 notification_log id를 넣어주세요")
	public ApiResponse<Void> readNotification(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("id") String id) {
		notificationLogService.updateNotificationLogAsRead(userDetails.getUser().getId(), id);
		return ApiResponse.success();
	}

}
