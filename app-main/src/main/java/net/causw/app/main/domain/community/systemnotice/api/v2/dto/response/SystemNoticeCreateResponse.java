package net.causw.app.main.domain.community.systemnotice.api.v2.dto.response;

import java.time.LocalDateTime;

public record SystemNoticeCreateResponse(
	String id,
	String title,
	String content,
	String authorName,
	LocalDateTime createdAt) {
}
