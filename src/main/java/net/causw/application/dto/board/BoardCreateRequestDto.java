package net.causw.application.dto.board;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardCreateRequestDto {

    @ApiModelProperty(value = "게시판 이름", example = "board_name")
    private String name;

    @ApiModelProperty(value = "게시판 설명", example = "board_description")
    private String description;

    @ApiModelProperty(value = "createRoleList(미완)", example = "[ 'ADMIN' ]")
    private List<String> createRoleList;

    @ApiModelProperty(value = "게시판 카테고리", example = "APP_NOTICE")
    private String category;

    @ApiModelProperty(value = "게시판이 속한 동아리 id", example = "uuid 형식의 String 값입니다(nullable).")
    private String circleId;

    public Optional<String> getCircleId() {
        return Optional.ofNullable(this.circleId);
    }
}
