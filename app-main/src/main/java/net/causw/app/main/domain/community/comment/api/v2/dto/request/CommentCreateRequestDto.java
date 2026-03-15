package net.causw.app.main.domain.community.comment.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentCreateRequestDto(
	@NotBlank(message = "댓글 내용을 입력해 주세요.") @Size(max = 255, message = "댓글 내용은 255자를 초과할 수 없습니다.") String content,

	@NotBlank(message = "댓글을 달 게시물이 선택되지 않았습니다.") @Size(max = 255, message = "선택된 게시물이 유효하지 않은 게시물입니다") String postId,

	@Schema(description = "익명글 여부", example = "False") @NotNull(message = "익명 여부가 선택되지 않았습니다.") Boolean isAnonymous) {

}