package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private String writerProfileImage;
    private BoardResponseDto board;
    private Boolean updatable;
    private Boolean deletable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Page<CommentResponseDto> commentList;

    private PostResponseDto(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            String writerProfileImage,
            BoardResponseDto board,
            Boolean updatable,
            Boolean deletable,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Page<CommentResponseDto> commentList
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.writerProfileImage = writerProfileImage;
        this.board = board;
        this.updatable = updatable;
        this.deletable = deletable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.commentList = commentList;
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
                BoardResponseDto.from(post.getBoard(), user.getRole()),
                updatable,
                deletable,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                null
        );
    }

    public static PostResponseDto from(
            PostDomainModel post,
            UserDomainModel user,
            Page<CommentResponseDto> commentList
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
                BoardResponseDto.from(post.getBoard(), user.getRole()),
                updatable,
                deletable,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                commentList
        );
    }
}
