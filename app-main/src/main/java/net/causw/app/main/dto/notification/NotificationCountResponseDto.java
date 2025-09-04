package net.causw.app.main.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NotificationCountResponseDto {
	@Schema(description = "읽지 않은 알림 개수", example = "1, 2, 3,...")
	private Integer notificationLogCount;
}

