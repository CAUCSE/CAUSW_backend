package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CircleWithBoardsResponseDto {
    private CircleResponseDto circle;
    private List<BoardOfCircleResponseDto> boardList;

    private CircleWithBoardsResponseDto(
            CircleResponseDto circle,
            List<BoardOfCircleResponseDto> boardList
    ) {
        this.circle = circle;
        this.boardList = boardList;
    }

    public static CircleWithBoardsResponseDto from(
            CircleResponseDto circle,
            List<BoardOfCircleResponseDto> boardList
    ) {
        return new CircleWithBoardsResponseDto(circle, boardList);
    }
}
