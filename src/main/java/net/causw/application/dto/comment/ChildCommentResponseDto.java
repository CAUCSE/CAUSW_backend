package net.causw.application.dto.comment;

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
    private String parentCommentId;

    private ChildCommentResponseDto(
            String id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            String tagUserName,
            String refChildComment,
            String writerName,
            Integer writerAdmissionYear,
            String writerProfileImage,
            Boolean updatable,
            Boolean deletable,
            String parentCommentId
    ) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.tagUserName = tagUserName;
        this.refChildComment = refChildComment;
        this.writerName = writerName;
        this.writerAdmissionYear = writerAdmissionYear;
        this.writerProfileImage = writerProfileImage;
        this.updatable = updatable;
        this.deletable = deletable;
        this.parentCommentId = parentCommentId;
    }

    public static ChildCommentResponseDto from(
            ChildCommentDomainModel comment,
            UserDomainModel user,
            BoardDomainModel board
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
        } else {
            if (board.getCircle().isPresent()) {
                boolean isLeader = user.getRole() == Role.LEADER_CIRCLE
                        && board.getCircle().get().getLeader()
                        .map(leader -> leader.getId().equals(user.getId()))
                        .orElse(false);
                if (isLeader) {
                    deletable = true;
                }
            } else {
                if (user.getRole() == Role.PRESIDENT) {
                    deletable = true;
                }
            }
        }

        if (comment.getIsDeleted()) {
            updatable = false;
            deletable = false;
            content = StaticValue.CONTENT_DELETED_COMMENT;
        }

        return new ChildCommentResponseDto(
                comment.getId(),
                content,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getIsDeleted(),
                comment.getTagUserName(),
                comment.getRefChildComment(),
                comment.getWriter().getName(),
                comment.getWriter().getAdmissionYear(),
                comment.getWriter().getProfileImage(),
                updatable,
                deletable,
                comment.getParentComment().getId()
        );
    }
}
