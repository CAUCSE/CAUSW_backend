package net.causw.app.main.domain.user.account.api.v1.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserFcmTokenResponseDto {
	private List<String> fcmToken;
}
