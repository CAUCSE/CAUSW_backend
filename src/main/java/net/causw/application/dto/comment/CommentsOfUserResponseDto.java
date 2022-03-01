package net.causw.application.dto.comment;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.CommentDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
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

    private CommentsOfUserResponseDto(
            String id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            String boardId,
            String boardName,
            String postId,
            String postName,
            String circleId,
            String circleName
    ) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.boardId = boardId;
        this.boardName = boardName;
        this.postId = postId;
        this.postName = postName;
        this.circleId = circleId;
        this.circleName = circleName;
    }

    public static CommentsOfUserResponseDto from(
            CommentDomainModel comment,
            String boardId,
            String boardName,
            String postId,
            String postName,
            String circleId,
            String circleName
    ) {
        return new CommentsOfUserResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getIsDeleted(),
                boardId,
                boardName,
                postId,
                postName,
                circleId,
                circleName
        );
    }
}
