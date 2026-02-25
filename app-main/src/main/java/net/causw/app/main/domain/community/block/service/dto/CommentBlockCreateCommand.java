package net.causw.app.main.domain.community.block.service.dto;

import net.causw.app.main.domain.user.account.entity.user.User;

public record CommentBlockCreateCommand(
	String targetUserId,
	String commentId,
	User blocker) {
}
