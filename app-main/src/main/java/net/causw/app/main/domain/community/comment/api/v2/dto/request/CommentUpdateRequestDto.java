package net.causw.app.main.domain.community.comment.api.v2.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequestDto(
	@NotBlank(message = "댓글 내용을 입력해 주세요.") String content) {

}
