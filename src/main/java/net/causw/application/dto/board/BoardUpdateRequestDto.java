package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardUpdateRequestDto {

    @Schema(description = "게시판 이름", example = "board_example")
    private String name;

    @Schema(description = "게시판 설명", example = "board_description")
    private String description;

    @Schema(description = "게시판에 글을 작성할 수 있는 권한 명단", example = "[ 'ADMIN' ]")
    private List<String> createRoleList;

    @Schema(description = "게시판 카테고리", example = "APP_NOTICE")
    private String category;
}
