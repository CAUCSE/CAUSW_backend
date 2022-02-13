package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CommentDomainModel {
    private String id;

    @NotBlank(message = "댓글 내용이 입력되지 않았습니다.")
    private String content;

    @NotNull(message = "댓글 상태가 입력되지 않았습니다.")
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @NotNull(message = "작성자가 입력되지 않았습니다.")
    private UserDomainModel writer;

    @NotNull(message = "게시글이 입력되지 않았습니다.")
    private String postId;
    private List<CommentDomainModel> childCommentList;

    private CommentDomainModel(
            String id,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserDomainModel writer,
            String postId,
            List<CommentDomainModel> childCommentList
    ) {
        this.id = id;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.writer = writer;
        this.postId = postId;
        this.childCommentList = childCommentList;
    }

    public static CommentDomainModel of(
            String content,
            UserDomainModel writer,
            String postId
    ) {
        return new CommentDomainModel(
                null,
                content,
                false,
                null,
                null,
                writer,
                postId,
                new ArrayList<>()
        );
    }

    public static CommentDomainModel of(
            String id,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserDomainModel writer,
            String postId
    ) {
        return new CommentDomainModel(
                id,
                content,
                isDeleted,
                createdAt,
                updatedAt,
                writer,
                postId,
                new ArrayList<>()
        );
    }

    public static CommentDomainModel of(
            String id,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserDomainModel writer,
            String postId,
            List<CommentDomainModel> childCommentList
    ) {
        return new CommentDomainModel(
                id,
                content,
                isDeleted,
                createdAt,
                updatedAt,
                writer,
                postId,
                childCommentList
        );
    }

    public void update(
            String content
    ) {
        this.content = content;
    }
}
