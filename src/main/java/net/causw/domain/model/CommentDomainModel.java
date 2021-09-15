package net.causw.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CommentDomainModel {
    private String id;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDomainModel writer;
    private PostDomainModel post;
    private CommentDomainModel parentComment; // 생성
    private List<CommentDomainModel> childCommentList; // 조회

    private CommentDomainModel(
            String id,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserDomainModel writer,
            PostDomainModel post,
            CommentDomainModel parentComment
    ) {
        this.id = id;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.writer = writer;
        this.post = post;
        this.parentComment = parentComment;
    }

    private CommentDomainModel(
            String id,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserDomainModel writer,
            PostDomainModel post
    ) {
        this.id = id;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.writer = writer;
        this.post = post;
    }

    private CommentDomainModel(
            String id,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserDomainModel writer,
            PostDomainModel post,
            List<CommentDomainModel> childCommentList
    ) {
        this.id = id;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.writer = writer;
        this.post = post;
        this.childCommentList = childCommentList;
    }

    public static CommentDomainModel of(
            String id,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserDomainModel writer,
            PostDomainModel post,
            CommentDomainModel parentComment
    ) {
        return new CommentDomainModel(
                id,
                content,
                isDeleted,
                createdAt,
                updatedAt,
                writer,
                post,
                parentComment
        );
    }

    public static CommentDomainModel of(
            String id,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserDomainModel writer,
            PostDomainModel post
    ) {
        return new CommentDomainModel(
                id,
                content,
                isDeleted,
                createdAt,
                updatedAt,
                writer,
                post
        );
    }

    public static CommentDomainModel of(
            String id,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserDomainModel writer,
            PostDomainModel post,
            List<CommentDomainModel> childCommentList
    ) {
        return new CommentDomainModel(
                id,
                content,
                isDeleted,
                createdAt,
                updatedAt,
                writer,
                post,
                childCommentList
        );
    }
}
