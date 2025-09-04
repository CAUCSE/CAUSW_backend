package net.causw.app.main.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.dto.notification.NotificationCountResponseDto;
import net.causw.app.main.dto.notification.NotificationResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.notification.NotificationLogService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications/log")
public class NotificationLogController {
	private final NotificationLogService notificationLogService;

	@GetMapping("/general")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "유저에게 온 일반 알람 조회", description = "유저의 일반 알림을 조회합니다.")
	public Page<NotificationResponseDto> getGeneralNotification(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
	) {
		return notificationLogService.getGeneralNotification(userDetails.getUser(), pageNum);
	}

	@GetMapping("/ceremony")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "유저에게 온 경조사 알람 조회", description = "유저의 경조사 알람을 조회합니다.")
	public Page<NotificationResponseDto> getCeremonyNotification(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
	) {
		return notificationLogService.getCeremonyNotification(userDetails.getUser(), pageNum);
	}

	@GetMapping("/general/top4")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "유저에게 온 일반 알람 조회"
		, description = "유저의 일반 알림을 조회합니다. <br>" +
		"해당 api는 웹상의 사이드 바 형태의 읽지 않은 알람 4개를 표시할 때 사용됩니다.")
	public List<NotificationResponseDto> getGeneralNotificationTop4(
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return notificationLogService.getGeneralNotificationTop4(userDetails.getUser());
	}

	@GetMapping("/ceremony/top4")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "유저에게 온 경조사 알람 조회"
		, description = "유저의 경조사 알림을 조회합니다. <br>" +
		"해당 api는 웹상의 사이드 바 형태의 읽지 않은 알람 4개를 표시할 때 사용됩니다.")
	public List<NotificationResponseDto> getCeremonyNotificationTop4(
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return notificationLogService.getCeremonyNotificationTop4(userDetails.getUser());
	}

	@PostMapping("/isRead/{id}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "유저에게 온 알람 읽음 여부 변경",
		description = "유저의 알람 조회 여부를 참으로 변경합니다<br> " +
			"id에는 notification_log id를 넣어주세요")
	public void readNotification(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("id") String id
	) {
		notificationLogService.readNotification(userDetails.getUser(), id);
	}

	@GetMapping("/count")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "유저에게 온 일반, 경조사 알림 중 읽지 않은 알림 총 개수 반환",
		description = "유저의 읽지 않은 알림 개수를 반환합니다.<br>" +
			"UI 상에서 10개 이상은 9+로 표기되기 때문에 10개까지 카운팅 되도록 하였습니다.")
	public NotificationCountResponseDto getNotificationLogCount(
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return notificationLogService.getNotificationLogCount(userDetails.getUser());
	}

}
