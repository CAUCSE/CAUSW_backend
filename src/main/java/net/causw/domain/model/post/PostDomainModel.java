package net.causw.domain.model.post;

import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.board.BoardDomainModel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDomainModel {
    private String id;

    @NotBlank(message = "게시글 제목이 입력되지 않았습니다.")
    private String title;

    @NotBlank(message = "게시글 내용이 입력되지 않았습니다.")
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
        return PostDomainModel.builder()
                .id(id)
                .title(title)
                .content(content)
                .writer(writer)
                .isDeleted(isDeleted)
                .board(board)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .attachmentList(attachmentList)
                .build();
    }

    public static PostDomainModel of(
            String title,
            String content,
            UserDomainModel writer,
            BoardDomainModel board,
            List<String> attachmentList
    ) {
        return PostDomainModel.builder()
                .title(title)
                .content(content)
                .writer(writer)
                .board(board)
                .attachmentList(attachmentList)
                .build();
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
