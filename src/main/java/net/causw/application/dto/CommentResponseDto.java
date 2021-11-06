package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class CommentResponseDto {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private String postId;
    private String writerId;
    private String writerName;
    private String writerProfileImage;
    private Boolean updatable;
    private Boolean deletable;
    private String parentCommentId;
    private List<CommentResponseDto> childCommentList;

    private CommentResponseDto(
            String id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            String postId,
            String writerId,
            String writerName,
            String writerProfileImage,
            Boolean updatable,
            Boolean deletable,
            String parentCommentId,
            List<CommentResponseDto> childCommentList
    ) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.postId = postId;
        this.writerId = writerId;
        this.writerName = writerName;
        this.writerProfileImage = writerProfileImage;
        this.updatable = updatable;
        this.deletable = deletable;
        this.parentCommentId = parentCommentId;
        this.childCommentList = childCommentList;
    }

    public static CommentResponseDto from(
            CommentDomainModel comment,
            UserDomainModel user,
            BoardDomainModel board
    ) {
        String parentCommentId = null;
        if (comment.getParentComment() != null) {
            parentCommentId = comment.getParentComment().getId();
        }

        boolean updatable = false;
        boolean deletable = false;

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

        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getIsDeleted(),
                comment.getPostId(),
                comment.getWriter().getId(),
                comment.getWriter().getName(),
                comment.getWriter().getProfileImage(),
                updatable,
                deletable,
                parentCommentId,
                comment.getChildCommentList()
                        .stream()
                        .map(commentDomainModel -> CommentResponseDto.from(commentDomainModel, user, board))
                        .collect(Collectors.toList())
        );
    }
}
