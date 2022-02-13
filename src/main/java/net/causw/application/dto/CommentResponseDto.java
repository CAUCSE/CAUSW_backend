package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
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
            Long numChildComment
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
    }

    public static CommentResponseDto from(
            CommentDomainModel comment,
            UserDomainModel user,
            BoardDomainModel board,
            Long numChildComment
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
                numChildComment
        );
    }
}
