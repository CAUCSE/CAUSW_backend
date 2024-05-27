package net.causw.domain.model.comment;

import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.user.UserDomainModel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
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

    private List<ChildCommentDomainModel> childCommentList;

    public static CommentDomainModel of(
            String content,
            UserDomainModel writer,
            String postId
    ) {
        return CommentDomainModel.builder()
                .content(content)
                .writer(writer)
                .postId(postId)
                .build();
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
        return CommentDomainModel.builder()
                .id(id)
                .content(content)
                .isDeleted(isDeleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .writer(writer)
                .postId(postId)
                .childCommentList(new ArrayList<>())
                .build();
    }

    public void update(
            String content
    ) {
        this.content = content;
    }

    public void setChildCommentList(List<ChildCommentDomainModel> childCommentList) {
        this.childCommentList = childCommentList;
    }
}
