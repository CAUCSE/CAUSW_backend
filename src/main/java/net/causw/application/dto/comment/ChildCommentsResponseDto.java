package net.causw.application.dto.comment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Setter
@Getter
public class ChildCommentsResponseDto {
    private CommentResponseDto parentComment;
    private Page<ChildCommentResponseDto> childComments;

    private ChildCommentsResponseDto(
            CommentResponseDto parentComment,
            Page<ChildCommentResponseDto> childComments
    ) {
        this.parentComment = parentComment;
        this.childComments = childComments;
    }

    public static ChildCommentsResponseDto from(
            CommentResponseDto parentComment,
            Page<ChildCommentResponseDto> childComments
    ) {
        return new ChildCommentsResponseDto(parentComment, childComments);
    }
}
