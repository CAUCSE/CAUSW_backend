package net.causw.application.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.post.PostDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
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

    public static UserPostResponseDto of(
            PostDomainModel post,
            String boardId,
            String boardName,
            String circleId,
            String circleName,
            Long numComment
    ) {
        return UserPostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .boardId(boardId)
                .boardName(boardName)
                .circleId(circleId)
                .circleName(circleName)
                .numComment(numComment)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
