package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChildCommentDomainModel {
    private final String DOMAIN = "답글";
    private String id;

    @NotBlank(message = "댓글 내용이 입력되지 않았습니다.")
    private String content;

    @NotNull(message = "댓글 상태가 입력되지 않았습니다.")
    private Boolean isDeleted;

    private String tagUserName;
    private String refChildComment;

    @NotNull(message = "작성자가 입력되지 않았습니다.")
    private UserDomainModel writer;

    @NotNull(message = "댓글이 입력되지 않았습니다.")
    private CommentDomainModel parentComment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ChildCommentDomainModel(
            String id,
            String content,
            Boolean isDeleted,
            String tagUserName,
            String refChildComment,
            UserDomainModel writer,
            CommentDomainModel parentComment,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.content = content;
        this.isDeleted = isDeleted;
        this.tagUserName = tagUserName;
        this.refChildComment = refChildComment;
        this.writer = writer;
        this.parentComment = parentComment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ChildCommentDomainModel of(
            String content,
            String tagUserName,
            String refChildComment,
            UserDomainModel writer,
            CommentDomainModel parentComment
    ) {
        return new ChildCommentDomainModel(
                null,
                content,
                false,
                tagUserName,
                refChildComment,
                writer,
                parentComment,
                null,
                null
        );
    }

    public static ChildCommentDomainModel of(
            String id,
            String content,
            Boolean isDeleted,
            String tagUserName,
            String refChildComment,
            UserDomainModel writer,
            CommentDomainModel parentComment,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new ChildCommentDomainModel(
                id,
                content,
                isDeleted,
                tagUserName,
                refChildComment,
                writer,
                parentComment,
                createdAt,
                updatedAt
        );
    }
}
