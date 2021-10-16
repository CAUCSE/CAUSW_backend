package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.UserDomainModel;

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
    private BoardResponseDto board;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponseDto> commentList;

    private PostResponseDto(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            BoardResponseDto board,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<CommentResponseDto> commentList
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.board = board;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.commentList = commentList;
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
                BoardResponseDto.from(post.getBoard(), user.getRole()),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                new ArrayList<>()
        );
    }

    public static PostResponseDto from(
            PostDomainModel post,
            UserDomainModel user,
            List<CommentResponseDto> commentList
    ) {
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsDeleted(),
                BoardResponseDto.from(post.getBoard(), user.getRole()),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                commentList
        );
    }
}
