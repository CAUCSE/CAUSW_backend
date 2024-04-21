package net.causw.application.dto.comment;

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

    public static ChildCommentsResponseDto of(
            CommentResponseDto parentComment,
            Page<ChildCommentResponseDto> childComments
    ) {
        return ChildCommentsResponseDto.builder()
                .parentComment(parentComment)
                .childComments(childComments)
                .build();
    }
}
