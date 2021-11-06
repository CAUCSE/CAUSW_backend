package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.Role;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
public class PostAllWithBoardResponseDto {
    private String boardId;
    private String boardName;
    private Boolean writable;
    private Page<PostAllResponseDto> post;

    private PostAllWithBoardResponseDto(
            String boardId,
            String boardName,
            Boolean writable,
            Page<PostAllResponseDto> post
    ) {
        this.boardId = boardId;
        this.boardName = boardName;
        this.writable = writable;
        this.post = post;
    }

    public static PostAllWithBoardResponseDto from(
            BoardDomainModel boardDomainModel,
            Role userRole,
            Page<PostAllResponseDto> post
    ) {
        return new PostAllWithBoardResponseDto(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getCreateRoleList().contains(userRole.getValue()),
                post
        );
    }
}
