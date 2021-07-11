package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.PostDomainModel;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostDetailDto {
    private String id;
    private String title;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BoardDetailDto board;

    private PostDetailDto(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            BoardDetailDto board
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.board = board;
    }

    public static PostDetailDto of(PostDomainModel post) {
        return new PostDetailDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsDeleted(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                BoardDetailDto.of(post.getBoard())
        );
    }
}
