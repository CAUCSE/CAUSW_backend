package net.causw.application.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Setter
@Getter
public class ChildCommentAllResponseDto {
    private CommentResponseDto parentComment;
    private Page<ChildCommentResponseDto> childComments;

    private ChildCommentAllResponseDto(CommentResponseDto parentComment, Page<ChildCommentResponseDto> childComments) {
        this.parentComment = parentComment;
        this.childComments = childComments;
    }

    public static ChildCommentAllResponseDto from(CommentResponseDto parentComment, Page<ChildCommentResponseDto> childComments) {
        return new ChildCommentAllResponseDto(parentComment, childComments);
    }
}
