package net.causw.app.main.domain.community.systemnotice.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeResult;

public record SystemNoticeResponse(
	String id,
	String content,
	String authorName,
	LocalDateTime createdAt,
	boolean isRead) {

	public static SystemNoticeResponse from(SystemNoticeResult result) {
		return new SystemNoticeResponse(
			result.id(),
			result.content(),
			result.authorName(),
			result.createdAt(),
			result.isRead());
	}
}
