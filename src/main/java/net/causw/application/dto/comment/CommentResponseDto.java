package net.causw.application.dto.comment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.StaticValue;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
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

    public static CommentResponseDto of(
            Comment comment,
            User user,
            Board board,
            Long numChildComment,
            List<ChildCommentResponseDto> childCommentList
    ) {
        String content = comment.getIsDeleted() ? StaticValue.CONTENT_DELETED_COMMENT : comment.getContent();
        boolean updatable = determineUpdatable(comment, user);
        boolean deletable = determineDeletable(comment, user, board);

        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(content)
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
                .build();
    }

    private static boolean determineUpdatable(Comment comment, User user) {
        if (comment.getIsDeleted()) return false;
        return user.getRole() == Role.ADMIN || comment.getWriter().getId().equals(user.getId());
    }

    private static boolean determineDeletable(Comment comment, User user, Board board) {
        if (comment.getIsDeleted()) return false;
        if (user.getRole() == Role.ADMIN || user.getRole().getValue().contains("PRESIDENT") || comment.getWriter().getId().equals(user.getId())) {
            return true;
        }
        return board.getCircle() != null && user.getRole().getValue().contains("LEADER_CIRCLE")
                && board.getCircle().getLeader().map(leader -> leader.getId().equals(user.getId())).orElse(false);
    }
}
