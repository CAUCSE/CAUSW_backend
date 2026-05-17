package net.causw.app.main.domain.notification.notification.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 FCM 푸시 알림 전송 요청")
public record AdminPushNotificationRequest(

	@NotBlank @Schema(description = "알림을 받을 유저 ID") String userId,

	@NotBlank @Schema(description = "알림 제목") String title,

	@NotBlank @Schema(description = "알림 내용") String body,

	@NotNull @Schema(description = "서비스 알림 저장 여부 (true: 알림함에 저장, false: 푸시만 전송)") Boolean saveNotification) {
}
