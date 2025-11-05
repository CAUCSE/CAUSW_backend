package net.causw.app.main.domain.moving.dto.user;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserFcmTokenResponseDto {
	private List<String> fcmToken;
}
