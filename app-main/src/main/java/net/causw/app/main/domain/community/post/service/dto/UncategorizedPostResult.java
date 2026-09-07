package net.causw.app.main.domain.community.post.service.dto;

import java.time.LocalDateTime;

public record UncategorizedPostResult(
	String postId,
	String title,
	String boardName,
	LocalDateTime createdAt) {
}
