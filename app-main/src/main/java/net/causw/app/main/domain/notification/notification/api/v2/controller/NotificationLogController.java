package net.causw.app.main.domain.notification.notification.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationCountResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.service.NotificationLogService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notifications/log")
public class NotificationLogController {
	private final NotificationLogService notificationLogService;
	//todo 반환 객체 적용 해야함
	@GetMapping("/latest")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "유저에게 온 최신 알람 조회", description = "유저의 최신 알림을 조회합니다. <br>" +
		"해당 api는 홈 화면에서 읽지 않은 최신 알람 1개를 표시할 때 사용됩니다.")
	public NotificationResponseDto getNotificationTop1(
		@AuthenticationPrincipal
		CustomUserDetails userDetails) {
		return notificationLogService.getNotificationTop1(userDetails.getUser());
	}

	@GetMapping("/count")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "유저에게 온 일반, 경조사 알림 중 읽지 않은 알림 총 개수 반환", description = "유저의 읽지 않은 알림 개수를 반환합니다.<br>" +
		"UI 상에서 10개 이상은 9+로 표기되기 때문에 10개까지 카운팅 되도록 하였습니다.")
	public NotificationCountResponseDto getNotificationLogCount(
		@AuthenticationPrincipal
		CustomUserDetails userDetails) {
		return notificationLogService.getNotificationLogCount(userDetails.getUser());
	}

}
