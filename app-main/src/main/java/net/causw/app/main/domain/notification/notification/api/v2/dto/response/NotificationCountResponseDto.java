package net.causw.app.main.domain.notification.notification.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;



@Builder
public record NotificationCountResponseDto(
	@Schema(description = "읽지 않은 알림 개수", example = "1, 2, 3,...")
	Integer notificationLogCount) {
}
