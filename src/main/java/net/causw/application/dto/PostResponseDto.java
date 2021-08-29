package net.causw.application.dto;

import net.causw.adapter.persistence.Post;

import java.time.LocalDateTime;

public class PostResponseDto {
    private String id;
    private String title;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String writerId;
    private String writerName;
    private String boardId;
    private String boardName;

    private PostResponseDto(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String writerId,
            String writerName,
            String boardId,
            String boardName
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.writerId = writerId;
        this.writerName = writerName;
        this.boardId = boardId;
        this.boardName = boardName;
    }

    public static PostResponseDto from(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsDeleted(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getWriter().getId(),
                post.getWriter().getName(),
                post.getBoard().getId(),
                post.getBoard().getName()
        );
    }

    public static PostResponseDto from(PostFullDto postDto) {
        return new PostResponseDto(
                postDto.getId(),
                postDto.getTitle(),
                postDto.getContent(),
                postDto.getIsDeleted(),
                postDto.getCreatedAt(),
                postDto.getUpdatedAt(),
                postDto.getWriter().getId(),
                postDto.getWriter().getName(),
                postDto.getBoard().getId(),
                postDto.getBoard().getName()
        );
    }
}
