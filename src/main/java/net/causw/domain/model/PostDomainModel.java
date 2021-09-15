package net.causw.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostDomainModel {
    private String id;
    private String title;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String boardId;
    private List<CommentDomainModel> commentList;

    private PostDomainModel(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String boardId
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.boardId = boardId;
    }

    private PostDomainModel(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String boardId,
            List<CommentDomainModel> commentList) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.boardId = boardId;
        this.commentList = commentList;
    }

    public static PostDomainModel of(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String boardId
    ) {
        return new PostDomainModel(
                id,
                title,
                content,
                isDeleted,
                createdAt,
                updatedAt,
                boardId
        );
    }

    public static PostDomainModel of(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String boardId,
            List<CommentDomainModel> commentList
    ) {
        return new PostDomainModel(
                id,
                title,
                content,
                isDeleted,
                createdAt,
                updatedAt,
                boardId,
                commentList
        );
    }
}
