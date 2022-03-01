package net.causw.application.dto.circle;

import lombok.Getter;
import lombok.Setter;
import net.causw.application.dto.board.BoardOfCircleResponseDto;

import java.util.List;

@Getter
@Setter
public class CircleBoardsResponseDto {
    private CircleResponseDto circle;
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
