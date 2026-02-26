package net.causw.app.main.domain.community.comment.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequestDto(
	@NotBlank(message = "댓글 내용을 입력해 주세요.") String content,

	@NotBlank(message = "댓글을 달 게시물이 선택되지 않았습니다.") String postId,

	@Schema(description = "익명글 여부", example = "False") Boolean isAnonymous) {

}