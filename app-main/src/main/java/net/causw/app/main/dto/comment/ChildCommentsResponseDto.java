package net.causw.app.main.dto.comment;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ChildCommentsResponseDto {
	private CommentResponseDto parentComment;
	private Page<ChildCommentResponseDto> childComments;

}
