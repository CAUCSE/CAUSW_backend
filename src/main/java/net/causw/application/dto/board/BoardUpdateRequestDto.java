package net.causw.application.dto.board;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "게시판 이름", example = "board_example")
    private String name;

    @ApiModelProperty(value = "게시판 설명", example = "board_description")
    private String description;


    @ApiModelProperty(value = "게시판에 글을 작성할 수 있는 권한 명단", example = "[ 'ADMIN' ]")
    private List<String> createRoleList;

    @ApiModelProperty(value = "게시판 카테고리", example = "APP_NOTICE")
    private String category;
}
