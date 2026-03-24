package net.causw.app.main.domain.user.relation.service.v2.dto;

import net.causw.app.main.domain.user.account.entity.user.User;

public record ChildCommentBlockCreateCommand(
	String childCommentId,
	User blocker) {
}
