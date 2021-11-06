package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.UserDomainModel;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class PostResponseDto {
    private String id;
    private String title;
    private String content;
    private Boolean isDeleted;
    private String writerProfileImage;
    private BoardResponseDto board;
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
            BoardResponseDto board,
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
        this.board = board;
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
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsDeleted(),
                post.getWriter().getProfileImage(),
                BoardResponseDto.from(post.getBoard(), user.getRole()),
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
            Page<CommentResponseDto> commentList
    ) {
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsDeleted(),
                post.getWriter().getProfileImage(),
                BoardResponseDto.from(post.getBoard(), user.getRole()),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                commentList,
                post.getBoard().getId(),
                post.getBoard().getName()
        );
    }
}
