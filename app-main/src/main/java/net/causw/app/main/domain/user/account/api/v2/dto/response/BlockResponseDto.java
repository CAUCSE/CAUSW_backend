package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.time.LocalDateTime;

public record BlockResponseDto(
	String blockId,
	String blockedUserId,
	String blockedUserName,
	LocalDateTime createdAt) {
}
