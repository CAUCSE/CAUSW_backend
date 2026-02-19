package net.causw.app.main.domain.community.post.service.v2.dto;

import net.causw.app.main.domain.user.account.entity.user.User;

public record PostDetailQuery(
	String postId,
	User viewer) {
}
