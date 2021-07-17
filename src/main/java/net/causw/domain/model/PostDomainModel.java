package net.causw.domain.model;

import lombok.Getter;

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

    public static PostDomainModel of(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            BoardDomainModel board
    ) {
        return new PostDomainModel(
                id,
                title,
                content,
                isDeleted,
                createdAt,
                updatedAt,
                board
        );
    }
}
