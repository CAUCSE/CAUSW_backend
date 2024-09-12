package net.causw.application.dto.comment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.comment.Comment;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CommentsOfUserResponseDto {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private String boardId;
    private String boardName;
    private String postId;
    private String postName;
    private String circleId;
    private String circleName;

}
