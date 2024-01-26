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

    @ApiModelProperty(value = "Board 이름", example = "board_example")
    private String name;

    @ApiModelProperty(value = "Board 설명", example = "board_description")
    private String description;


    @ApiModelProperty(value = "createRoleList(미완)", example = "[ 'ADMIN' ]")
    private List<String> createRoleList;

    @ApiModelProperty(value = "Board 카테고리", example = "APP_NOTICE")
    private String category;
}
