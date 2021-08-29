package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.Post;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostFullDto {
    private String id;
    private String title;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserFullDto writer;
    private BoardDetailDto board;

    private PostFullDto(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserFullDto writer,
            BoardDetailDto board
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.writer = writer;
        this.board = board;
    }

    public static PostFullDto from(Post post) {
        return new PostFullDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsDeleted(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                UserFullDto.from(post.getWriter()),
                BoardDetailDto.from(post.getBoard())
        );
    }
}
