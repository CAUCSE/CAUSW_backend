package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserFcmToken(
	@Schema(description = "유저에게 등록할 fcmToken") String fcmToken) {
}
