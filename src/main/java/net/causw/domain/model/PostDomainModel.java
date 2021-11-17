package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class PostDomainModel {
    private final String DOMAIN = "게시글";
    private String id;

    @NotBlank(message = "게시글 제목이 입력되지 않았습니다.")
    private String title;
    private String content;

    @NotNull(message = "작성자가 입력되지 않았습니다.")
    private UserDomainModel writer;

    @NotNull(message = "게시글 상태가 입력되지 않았습니다.")
    private Boolean isDeleted;

    @NotNull(message = "게시판이 입력되지 않았습니다.")
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
