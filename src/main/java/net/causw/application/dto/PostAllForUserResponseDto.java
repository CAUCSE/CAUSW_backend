package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.PostDomainModel;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostAllForUserResponseDto {
    private String id;
    private String title;
    private String boardId;
    private String boardName;
    private String circleId;
    private String circleName;
    private Long numComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PostAllForUserResponseDto(
            String id,
            String title,
            String boardId,
            String boardName,
            String circleId,
            String circleName,
            Long numComment,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.boardId = boardId;
        this.boardName = boardName;
        this.circleId = circleId;
        this.circleName = circleName;
        this.numComment = numComment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PostAllForUserResponseDto from(
            PostDomainModel post,
            String boardId,
            String boardName,
            String circleId,
            String circleName,
            Long numComment
    ) {
        return new PostAllForUserResponseDto(
                post.getId(),
                post.getTitle(),
                boardId,
                boardName,
                circleId,
                circleName,
                numComment,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
