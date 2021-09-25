package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class PostDomainModel {
    private String id;

    @NotBlank(message = "Title is blank")
    private String title;

    @NotBlank(message = "Content is blank")
    private String content;

    @NotNull(message = "Writer is null")
    private UserDomainModel writer;
    private Boolean isDeleted;
    private BoardDomainModel board;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PostDomainModel(
            String id,
            String title,
            String content,
            UserDomainModel writer,
            Boolean isDeleted,
            BoardDomainModel board,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.isDeleted = isDeleted;
        this.board = board;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PostDomainModel of(
            String id,
            String title,
            String content,
            UserDomainModel writer,
            Boolean isDeleted,
            BoardDomainModel board,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new PostDomainModel(
                id,
                title,
                content,
                writer,
                isDeleted,
                board,
                createdAt,
                updatedAt
        );
    }

    public static PostDomainModel of(
            String title,
            String content,
            UserDomainModel writer,
            BoardDomainModel board
    ) {
        return new PostDomainModel(
                null,
                title,
                content,
                writer,
                false,
                board,
                null,
                null
        );
    }
}
