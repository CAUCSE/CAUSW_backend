package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.util.Set;

import lombok.Builder;

@Builder
public record UserFcmTokenResponse(
	Set<String> fcmToken
) {
}
