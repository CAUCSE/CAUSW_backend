package net.causw.app.main.domain.community.systemnotice.service.dto;

import java.time.LocalDateTime;

public record SystemNoticeCreateResult(
	String id,
	String title,
	String content,
	String authorName,
	LocalDateTime createdAt) {
}
