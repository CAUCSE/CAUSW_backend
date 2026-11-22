package net.causw.app.main.domain.notification.notification.api.v2.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.notification.notification.api.v2.dto.request.UpdateBoardSubscribeRequest;
import net.causw.app.main.domain.notification.notification.api.v2.dto.request.UpdateNotificationSettingRequest;
import net.causw.app.main.domain.notification.notification.api.v2.dto.request.UpdateOfficialBoardSubscribeRequest;
import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationSettingResponse;
import net.causw.app.main.domain.notification.notification.service.NotificationSettingService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notification-settings")
@Tag(name = "NotificationSetting Public v2", description = "알림 설정 조회 및 수정 API")
public class NotificationSettingController {

	private final NotificationSettingService notificationSettingService;

	@GetMapping
	@Operation(summary = "알림 설정 전체 조회", description = "개인별 고정 토글(커뮤니티/경조사/서비스 공지), 소식 알림 설정(전체 토글·채널·성격), "
		+ "소통 게시판 구독 목록을 한 번에 반환합니다. "
		+ "DB에 저장된 값이 없으면 서버 기본값을 반환합니다. (소식 게시판: 구독, 소통 게시판: 미구독)")
	public ApiResponse<NotificationSettingResponse> getAllSettings(
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ApiResponse.success(
			NotificationSettingResponse.from(
				notificationSettingService.getAllSettings(userDetails.getUserId())));
	}

	@PatchMapping
	@Operation(summary = "개인별 알림 설정 수정", description = "요청에 포함된 토글만 업데이트합니다(부분 업데이트). "
		+ "포함되지 않은 필드는 변경되지 않습니다.")
	public ApiResponse<Void> updateUserSettings(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody UpdateNotificationSettingRequest request) {

		notificationSettingService.updateUserSettings(
			userDetails.getUserId(), request.toCommand());
		return ApiResponse.success();
	}

	@PatchMapping("/official-boards/{boardId}")
	@Operation(summary = "공식계정 게시판 구독 수정", deprecated = true, description = "is_notice=true 인 게시판의 구독 상태를 수정합니다. "
		+ "기존 앱 호환용이며, 신규 클라이언트는 PATCH /api/v2/notification-settings/boards/{boardId}를 사용합니다.")
	public ApiResponse<Void> updateOfficialBoardSubscribe(
		@PathVariable String boardId,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody UpdateOfficialBoardSubscribeRequest request) {

		notificationSettingService.updateOfficialBoardSubscribe(
			userDetails.getUserId(), boardId, request.subscribed());
		return ApiResponse.success();
	}

	@PatchMapping("/boards/{boardId}")
	@Operation(summary = "게시판 구독 수정", description = "소식(채널)·소통 게시판의 알림 구독 상태를 수정합니다. "
		+ "읽기 권한이 있는 게시판만 수정할 수 있으며, 시스템 공지 게시판은 수정할 수 없습니다.")
	public ApiResponse<Void> updateBoardSubscribe(
		@PathVariable String boardId,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody UpdateBoardSubscribeRequest request) {

		notificationSettingService.updateBoardSubscribe(
			userDetails.getUserId(), boardId, request.subscribed());
		return ApiResponse.success();
	}
}
