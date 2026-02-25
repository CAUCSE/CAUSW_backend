package net.causw.app.main.domain.community.block.api.v2.dto.response;

import java.time.LocalDateTime;

public record BlockResponse(
	String blockId,
	String blockedUserId,
	String blockedUserName,
	LocalDateTime createdAt) {
}
