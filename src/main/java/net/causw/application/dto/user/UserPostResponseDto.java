package net.causw.application.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.post.Post;
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

}
