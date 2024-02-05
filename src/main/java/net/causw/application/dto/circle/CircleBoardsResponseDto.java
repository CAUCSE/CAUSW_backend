package net.causw.application.dto.circle;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import net.causw.application.dto.board.BoardOfCircleResponseDto;

import java.util.List;

@Getter
@Setter
public class CircleBoardsResponseDto {

    @ApiModelProperty(value ="CircleResponseDto", example = "동아리 responseDTO 객체를 반환합니다.")
    private CircleResponseDto circle;

    @ApiModelProperty(value ="List<BoardOfCircleResponseDto", example = "동아리의 속한 게시판 목록을 List<BoardOfCircleResponseDto 객체(리스트)로 반환합니다.")
    private List<BoardOfCircleResponseDto> boardList;

    private CircleBoardsResponseDto(
            CircleResponseDto circle,
            List<BoardOfCircleResponseDto> boardList
    ) {
        this.circle = circle;
        this.boardList = boardList;
    }

    public static CircleBoardsResponseDto from(
            CircleResponseDto circle,
            List<BoardOfCircleResponseDto> boardList
    ) {
        return new CircleBoardsResponseDto(circle, boardList);
    }
}
