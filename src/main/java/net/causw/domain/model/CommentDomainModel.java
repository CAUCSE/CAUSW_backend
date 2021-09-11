package net.causw.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDomainModel {
    private String id;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDomainModel writer;
    private PostDomainModel post;
    private CommentDomainModel parentComment;
    private List<CommentDomainModel> childCommentList;

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
