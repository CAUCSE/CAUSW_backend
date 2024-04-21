package net.causw.application.dto.comment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.comment.CommentDomainModel;

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

    public static CommentsOfUserResponseDto of(
            CommentDomainModel comment,
            String boardId,
            String boardName,
            String postId,
            String postName,
            String circleId,
            String circleName
    ) {
        return CommentsOfUserResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isDeleted(comment.getIsDeleted())
                .boardId(boardId)
                .boardName(boardName)
                .postId(postId)
                .postName(postName)
                .circleId(circleId)
                .circleName(circleName)
                .build();
    }
}
