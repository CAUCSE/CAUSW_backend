package net.causw.app.main.domain.community.comment.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChildCommentCreateRequestDto(
	@NotBlank(message = "대댓글 내용을 입력해 주세요.") @Size(max = 255, message = "대댓글 내용은 255자를 초과할 수 없습니다.") String content,

	@NotBlank(message = "답변할 댓글을 선택해 주세요.") @Size(max = 255, message = "선택된 댓글이 유효하지 않은 댓글입니다") String parentCommentId,

	@Schema(description = "익명글 여부", example = "False") @NotNull(message = "익명 여부가 선택되지 않았습니다.") Boolean isAnonymous) {
}
