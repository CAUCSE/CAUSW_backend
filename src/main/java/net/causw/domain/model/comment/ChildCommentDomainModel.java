package net.causw.domain.model.comment;

import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.user.UserDomainModel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Builder
public class ChildCommentDomainModel {
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

    public static ChildCommentDomainModel of(
            String content,
            String tagUserName,
            String refChildComment,
            UserDomainModel writer,
            CommentDomainModel parentComment
    ) {
        return ChildCommentDomainModel.builder()
                .content(content)
                .tagUserName(tagUserName)
                .refChildComment(refChildComment)
                .writer(writer)
                .parentComment(parentComment)
                .build();
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
        return ChildCommentDomainModel.builder()
                .id(id)
                .content(content)
                .isDeleted(isDeleted)
                .tagUserName(tagUserName)
                .refChildComment(refChildComment)
                .writer(writer)
                .parentComment(parentComment)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public void update(
            String content
    ) {
        this.content = content;
    }
}
