package net.causw.application.dto.comment;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.comment.CommentDomainModel;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
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
    private Long numChildComment;
    private List<ChildCommentResponseDto> childCommentList;

    private CommentResponseDto(
            String id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            String postId,
            String writerName,
            Integer writerAdmissionYear,
            String writerProfileImage,
            Boolean updatable,
            Boolean deletable,
            Long numChildComment,
            List<ChildCommentResponseDto> childCommentList
    ) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.postId = postId;
        this.writerName = writerName;
        this.writerAdmissionYear = writerAdmissionYear;
        this.writerProfileImage = writerProfileImage;
        this.updatable = updatable;
        this.deletable = deletable;
        this.numChildComment = numChildComment;
        this.childCommentList = childCommentList;
    }

    public static CommentResponseDto from(
            CommentDomainModel comment,
            UserDomainModel user,
            BoardDomainModel board,
            Long numChildComment,
            List<ChildCommentResponseDto> childCommentList
    ) {
        boolean updatable = false;
        boolean deletable = false;
        String content = comment.getContent();

        if (user.getRole() == Role.ADMIN) {
            updatable = true;
            deletable = true;
        } else if (comment.getWriter().getId().equals(user.getId())) {
            updatable = true;
            deletable = true;
        } else if (user.getRole().getValue().contains("PRESIDENT")) {
            deletable = true;
        } else {
            if (board.getCircle().isPresent()) {
                boolean isLeader = user.getRole().getValue().contains("LEADER_CIRCLE")
                        && board.getCircle().get().getLeader()
                        .map(leader -> leader.getId().equals(user.getId()))
                        .orElse(false);
                if (isLeader) {
                    deletable = true;
                }
            }
        }

        if (comment.getIsDeleted()) {
            updatable = false;
            deletable = false;
            content = StaticValue.CONTENT_DELETED_COMMENT;
        }

        return new CommentResponseDto(
                comment.getId(),
                content,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getIsDeleted(),
                comment.getPostId(),
                comment.getWriter().getName(),
                comment.getWriter().getAdmissionYear(),
                comment.getWriter().getProfileImage(),
                updatable,
                deletable,
                numChildComment,
                childCommentList
        );
    }
}
