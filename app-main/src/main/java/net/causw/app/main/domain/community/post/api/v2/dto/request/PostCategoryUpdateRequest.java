package net.causw.app.main.domain.community.post.api.v2.dto.request;

import net.causw.app.main.domain.community.post.enums.PostCategory;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 성격 수정 요청")
public record PostCategoryUpdateRequest(
	@Schema(description = "지정할 성격. null이면 미분류로 되돌립니다.", example = "RECRUIT", nullable = true) PostCategory category) {
}
