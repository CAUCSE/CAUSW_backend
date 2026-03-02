package net.causw.app.main.domain.user.relation.service.v2.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record BlockCreateResult(
	String blockId,
	String blockedUserId,
	String blockedUserName,
	LocalDateTime createdAt) {
}
