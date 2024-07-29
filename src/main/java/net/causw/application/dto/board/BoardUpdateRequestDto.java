package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "게시판 이름은 필수 입력값입니다.")
    private String name;

    @Schema(description = "게시판 설명", example = "board_description")
    private String description;

    @Schema(description = "게시판에 글을 작성할 수 있는 권한 명단", example = "[ 'ADMIN' ]")
    @NotBlank(message = "게시판 글에 작성할 수 있는 권한 명단은 필수 입력값입니다.")
    private List<String> createRoleList;

    @Schema(description = "게시판 카테고리", example = "APP_NOTICE")
    @NotBlank(message = "게시판 카테고리는 필수 입력값입니다.")
    private String category;
}
