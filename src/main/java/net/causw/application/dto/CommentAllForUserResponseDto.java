package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.CommentDomainModel;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommentAllForUserResponseDto {
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

    private CommentAllForUserResponseDto(
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

    public static CommentAllForUserResponseDto from(
            CommentDomainModel comment,
            String boardId,
            String boardName,
            String postId,
            String postName,
            String circleId,
            String circleName
    ) {
        return new CommentAllForUserResponseDto(
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
