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
    private String boardName;
    private String postName;
    private String circleName;

    private CommentAllForUserResponseDto(
            String id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            String boardName,
            String postName,
            String circleName
    ) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.boardName = boardName;
        this.postName = postName;
        this.circleName = circleName;
    }

    public static CommentAllForUserResponseDto from(
            CommentDomainModel comment,
            String boardName,
            String postName,
            String circleName
    ) {
        return new CommentAllForUserResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getIsDeleted(),
                boardName,
                postName,
                circleName
        );
    }
}
