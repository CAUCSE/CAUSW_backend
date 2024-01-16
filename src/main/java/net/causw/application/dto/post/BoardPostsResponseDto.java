package net.causw.application.dto.post;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.enums.Role;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class BoardPostsResponseDto {
    private String boardId;
    private String boardName;
    private Boolean writable;
    private Boolean isFavorite;
    private Page<PostsResponseDto> post;

    private BoardPostsResponseDto(
            String boardId,
            String boardName,
            Boolean writable,
            Boolean isFavorite,
            Page<PostsResponseDto> post
    ) {
        this.boardId = boardId;
        this.boardName = boardName;
        this.writable = writable;
        this.isFavorite = isFavorite;
        this.post = post;
    }

    public static BoardPostsResponseDto from(
            BoardDomainModel boardDomainModel,
            Role userRole,
            Boolean isFavorite,
            Page<PostsResponseDto> post
    ) {
        return new BoardPostsResponseDto(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getCreateRoleList().contains(userRole.getValue()),
                isFavorite,
                post
        );
    }
}
