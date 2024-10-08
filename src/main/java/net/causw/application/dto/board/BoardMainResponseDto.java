package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.application.dto.post.PostContentDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardMainResponseDto {
    @Schema(description = "게시판 아이디", example = "board_id")
    private String boardId;

    @Schema(description = "필수 게시판인지 확인", example = "default_board")
    private Boolean isDefault;

    @Schema(description = "게시판 이름", example = "board_name")
    private String boardName;

    @Schema(description = "최근 게시글 3개", example = "board_recent_post")
    private List<PostContentDto> contents;
}