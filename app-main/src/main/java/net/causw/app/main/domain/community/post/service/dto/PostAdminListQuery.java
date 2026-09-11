package net.causw.app.main.domain.community.post.service.dto;

import net.causw.app.main.domain.community.post.enums.PostAdminStatus;
import net.causw.app.main.domain.community.post.enums.PostCategory;

public record PostAdminListQuery(
	String boardId,
	PostCategory category,
	PostAdminStatus status,
	String keyword,
	String writerKeyword) {
}
