package net.causw.app.main.domain.community.comment.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequestDto(
	@NotBlank(message = "댓글 내용을 입력해 주세요.") String content,

	@NotBlank(message = "게시물 id를 입력해 주세요.") String postId,

	@Schema(description = "익명글 여부", example = "False") Boolean isAnonymous) {

}