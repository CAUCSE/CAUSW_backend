package net.causw.app.main.dto.comment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Setter
@Getter
@Builder
public class ChildCommentsResponseDto {
    private CommentResponseDto parentComment;
    private Page<ChildCommentResponseDto> childComments;

}
