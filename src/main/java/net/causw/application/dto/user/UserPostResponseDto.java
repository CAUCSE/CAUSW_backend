package net.causw.application.dto.user;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.post.PostDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserPostResponseDto {
    private String id;
    private String title;
    private String boardId;
    private String boardName;
    private String circleId;
    private String circleName;
    private Long numComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserPostResponseDto(
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

    public static UserPostResponseDto from(
            PostDomainModel post,
            String boardId,
            String boardName,
            String circleId,
            String circleName,
            Long numComment
    ) {
        return new UserPostResponseDto(
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
