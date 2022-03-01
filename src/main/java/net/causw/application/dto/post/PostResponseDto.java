package net.causw.application.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostResponseDto {
    private String id;
    private String title;
    private String content;
    private Boolean isDeleted;
    private String writerName;
    private Integer writerAdmissionYear;
    private String writerProfileImage;
    private Long numComment;
    private BoardResponseDto board;
    private Boolean updatable;
    private Boolean deletable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Page<CommentResponseDto> commentList;

    private String boardId;
    private String boardName;

    private PostResponseDto(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            String writerProfileImage,
            String writerName,
            Integer writerAdmissionYear,
            Long numComment,
            BoardResponseDto board,
            Boolean updatable,
            Boolean deletable,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Page<CommentResponseDto> commentList,
            String boardId,
            String boardName
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.writerProfileImage = writerProfileImage;
        this.writerName = writerName;
        this.writerAdmissionYear = writerAdmissionYear;
        this.numComment = numComment;
        this.board = board;
        this.updatable = updatable;
        this.deletable = deletable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.commentList = commentList;
        this.boardId = boardId;
        this.boardName = boardName;
    }

    public static PostResponseDto from(
            PostDomainModel post,
            UserDomainModel user
    ) {
        boolean updatable = false;
        boolean deletable = false;

        if (user.getRole() == Role.ADMIN) {
            updatable = true;
            deletable = true;
        } else if (post.getWriter().getId().equals(user.getId())) {
            updatable = true;
            deletable = true;
        } else {
            if (post.getBoard().getCircle().isPresent()) {
                boolean isLeader = user.getRole() == Role.LEADER_CIRCLE
                        && post.getBoard().getCircle().get().getLeader()
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

        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsDeleted(),
                post.getWriter().getProfileImage(),
                post.getWriter().getName(),
                post.getWriter().getAdmissionYear(),
                0L,
                BoardResponseDto.from(post.getBoard(), user.getRole()),
                updatable,
                deletable,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                null,
                null,
                null
        );
    }

    public static PostResponseDto from(
            PostDomainModel post,
            UserDomainModel user,
            Page<CommentResponseDto> commentList,
            Long numComment
    ) {
        boolean updatable = false;
        boolean deletable = false;

        if (user.getRole() == Role.ADMIN) {
            updatable = true;
            deletable = true;
        } else if (post.getWriter().getId().equals(user.getId())) {
            updatable = true;
            deletable = true;
        } else {
            if (post.getBoard().getCircle().isPresent()) {
                boolean isLeader = user.getRole() == Role.LEADER_CIRCLE
                        && post.getBoard().getCircle().get().getLeader()
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

        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsDeleted(),
                post.getWriter().getProfileImage(),
                post.getWriter().getName(),
                post.getWriter().getAdmissionYear(),
                numComment,
                BoardResponseDto.from(post.getBoard(), user.getRole()),
                updatable,
                deletable,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                commentList,
                post.getBoard().getId(),
                post.getBoard().getName()
        );
    }
}
