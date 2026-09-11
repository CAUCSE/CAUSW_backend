package net.causw.app.main.domain.community.post.api.v2.dto.request;

import net.causw.app.main.domain.community.post.enums.PostAdminStatus;
import net.causw.app.main.domain.community.post.enums.PostCategory;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostAdminListCondition(
	@Schema(description = "게시판 ID") String boardId,
	@Schema(description = "게시글 성격. null이면 전체", example = "RECRUIT") PostCategory category,
	@Schema(description = "게시글 상태. null이면 전체", example = "VISIBLE") PostAdminStatus status,
	@Schema(description = "제목 또는 본문 검색어") String keyword,
	@Schema(description = "작성자 이름 또는 닉네임 검색어") String writerKeyword) {
}
