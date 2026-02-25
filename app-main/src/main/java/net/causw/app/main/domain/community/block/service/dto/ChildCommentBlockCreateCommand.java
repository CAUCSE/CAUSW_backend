package net.causw.app.main.domain.community.block.service.dto;

import net.causw.app.main.domain.user.account.entity.user.User;

public record ChildCommentBlockCreateCommand(
	String targetUserId,
	String childCommentId,
	User blocker) {
}
