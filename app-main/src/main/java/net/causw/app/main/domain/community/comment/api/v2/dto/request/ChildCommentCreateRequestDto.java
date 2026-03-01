package net.causw.app.main.domain.community.comment.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChildCommentCreateRequestDto(
	@NotBlank(message = "대댓글 내용을 입력해 주세요.") String content,

	@NotBlank(message = "답변할 댓글을 선택해 주세요.") String parentCommentId,

	@Schema(description = "익명글 여부", example = "False") Boolean isAnonymous) {
}
