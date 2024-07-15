package net.causw.application.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.board.Board;
import net.causw.domain.model.enums.Role;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BoardPostsResponseDto {

    @Schema(description = "게시판 id", example = "uuid 형식의 String 값입니다.")
    private String boardId;

    @Schema(description = "게시판 이름", example = "게시판 이름입니다.")
    private String boardName;

    @Schema(description = "게시글 작성 가능 여부", example = "true")
    private Boolean writable;

    @Schema(description = "즐겨찾기 게시판 여부", example = "false")
    private Boolean isFavorite;

    @Schema(description = "게시글 정보입니다")
    private Page<PostsResponseDto> post;

    // FIXME: 리팩토링 후 삭제예정
    public static BoardPostsResponseDto of(
            Board board,
            Role userRole,
            Boolean isFavorite,
            Page<PostsResponseDto> post
    ) {
        List<String> roles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));
        return BoardPostsResponseDto.builder()
                .boardId(board.getId())
                .boardName(board.getName())
                .writable(roles.stream().anyMatch(str -> userRole.getValue().contains(str)))
                .isFavorite(isFavorite)
                .post(post)
                .build();
    }
}
