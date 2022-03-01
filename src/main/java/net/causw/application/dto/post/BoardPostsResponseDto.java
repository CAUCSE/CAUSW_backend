package net.causw.application.dto.post;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.Role;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class BoardPostsResponseDto {
    private String boardId;
    private String boardName;
    private Boolean writable;
    private Page<PostsResponseDto> post;

    private BoardPostsResponseDto(
            String boardId,
            String boardName,
            Boolean writable,
            Page<PostsResponseDto> post
    ) {
        this.boardId = boardId;
        this.boardName = boardName;
        this.writable = writable;
        this.post = post;
    }

    public static BoardPostsResponseDto from(
            BoardDomainModel boardDomainModel,
            Role userRole,
            Page<PostsResponseDto> post
    ) {
        return new BoardPostsResponseDto(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getCreateRoleList().contains(userRole.getValue()),
                post
        );
    }
}
