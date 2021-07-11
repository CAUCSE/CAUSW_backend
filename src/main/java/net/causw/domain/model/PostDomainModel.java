package net.causw.domain.model;

import lombok.Getter;
import net.causw.infra.Post;

import java.time.LocalDateTime;

@Getter
public class PostDomainModel {
    private String id;
    private String title;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BoardDomainModel board;

    private PostDomainModel(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            BoardDomainModel board) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.board = board;
    }

    public static PostDomainModel of(Post post) {
        return new PostDomainModel(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsDeleted(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                BoardDomainModel.of(post.getBoard())
        );
    }
}
