package net.causw.app.main.domain.community.systemnotice.service.dto;

import java.time.LocalDateTime;

public record SystemNoticeResult(
	String id,
	String content,
	String authorName,
	LocalDateTime createdAt,
	boolean isRead) {
}
