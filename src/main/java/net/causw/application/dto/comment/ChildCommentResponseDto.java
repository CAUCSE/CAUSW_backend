package net.causw.application.dto.comment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.comment.ChildCommentDomainModel;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChildCommentResponseDto {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private String tagUserName;
    private String refChildComment;
    private String writerName;
    private Integer writerAdmissionYear;
    private String writerProfileImage;
    private Boolean updatable;
    private Boolean deletable;

    public static ChildCommentResponseDto of(
            ChildCommentDomainModel comment,
            UserDomainModel user,
            BoardDomainModel board
    ) {
        boolean updatable = false;
        boolean deletable = false;
        String content = comment.getContent();

        if (user.getRole() == Role.ADMIN || comment.getWriter().getId().equals(user.getId())) {
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

        return ChildCommentResponseDto.builder()
                .id(comment.getId())
                .content(content)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isDeleted(comment.getIsDeleted())
                .tagUserName(comment.getTagUserName())
                .refChildComment(comment.getRefChildComment())
                .writerName(comment.getWriter().getName())
                .writerAdmissionYear(comment.getWriter().getAdmissionYear())
                .writerProfileImage(comment.getWriter().getProfileImage())
                .updatable(updatable)
                .deletable(deletable)
                .build();
    }
}
