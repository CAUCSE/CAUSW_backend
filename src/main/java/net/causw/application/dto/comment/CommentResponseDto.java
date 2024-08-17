package net.causw.application.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.comment.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CommentResponseDto {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private String postId;
    private String writerName;
    private Integer writerAdmissionYear;
    private String writerProfileImage;
    private Boolean updatable;
    private Boolean deletable;

    @Schema(description = "익명글 여부", example = "False")
    private Boolean isAnonymous;

    private Long numChildComment;
    private List<ChildCommentResponseDto> childCommentList;

    public static CommentResponseDto of(
            Comment comment,
            Long numChildComment,
            List<ChildCommentResponseDto> childCommentList,
            boolean updatable,
            boolean deletable,
            boolean isAnonymous
    ){
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isDeleted(comment.getIsDeleted())
                .postId(comment.getPost().getId())
                .writerName(comment.getWriter().getName())
                .writerAdmissionYear(comment.getWriter().getAdmissionYear())
                .writerProfileImage(comment.getWriter().getProfileImage())
                .updatable(updatable)
                .deletable(deletable)
                .numChildComment(numChildComment)
                .childCommentList(childCommentList)
                .isAnonymous(isAnonymous)
                .build();
    }
}
