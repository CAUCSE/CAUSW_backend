package net.causw.application.dto.post;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.enums.Role;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class BoardPostsResponseDto {

    @ApiModelProperty(value = "게시판 id", example = "uuid 형식의 String 값입니다.")
    private String boardId;

    @ApiModelProperty(value = "게시판 이름", example = "게시판 이름입니다.")
    private String boardName;

    @ApiModelProperty(value = "게시글 작성 가능여부", example = "true")
    private Boolean writable;

    @ApiModelProperty(value = "즐겨찾기 게시판 여부", example = "false")
    private Boolean isFavorite;

    @ApiModelProperty(value = "게시글 정보입니다", example = "게시글 정보입니다")
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
                boardDomainModel.getCreateRoleList().stream().anyMatch(str -> userRole.getValue().contains(str)),
                isFavorite,
                post
        );
    }
}
