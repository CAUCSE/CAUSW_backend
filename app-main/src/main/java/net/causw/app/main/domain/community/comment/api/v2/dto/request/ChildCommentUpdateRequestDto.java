package net.causw.app.main.domain.community.comment.api.v2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChildCommentUpdateRequestDto(
	@NotBlank(message = "대댓글 내용을 입력해 주세요.") @Size(max = 255, message = "대댓글 내용은 255자를 초과할 수 없습니다.") String content) {
}
