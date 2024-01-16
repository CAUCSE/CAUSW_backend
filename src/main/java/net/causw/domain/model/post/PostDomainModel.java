package net.causw.domain.model.post;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.board.BoardDomainModel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostDomainModel {
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

    private List<String> attachmentList;
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
            LocalDateTime updatedAt,
            List<String> attachmentList
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.isDeleted = isDeleted;
        this.board = board;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.attachmentList = attachmentList;
    }

    public static PostDomainModel of(
            String id,
            String title,
            String content,
            UserDomainModel writer,
            Boolean isDeleted,
            BoardDomainModel board,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<String> attachmentList
    ) {
        return new PostDomainModel(
                id,
                title,
                content,
                writer,
                isDeleted,
                board,
                createdAt,
                updatedAt,
                attachmentList
        );
    }

    public static PostDomainModel of(
            String title,
            String content,
            UserDomainModel writer,
            BoardDomainModel board,
            List<String> attachmentList
    ) {
        return new PostDomainModel(
                null,
                title,
                content,
                writer,
                false,
                board,
                null,
                null,
                attachmentList
        );
    }

    public void update(
            String title,
            String content,
            List<String> attachmentList
    ) {
        this.title = title;
        this.content = content;
        this.attachmentList = attachmentList;
    }
}
