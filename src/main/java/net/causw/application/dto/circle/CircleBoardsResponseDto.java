package net.causw.application.dto.circle;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.application.dto.board.BoardOfCircleResponseDto;
import net.causw.application.dto.util.CircleServiceDtoMapper;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CircleBoardsResponseDto {

    @ApiModelProperty(value ="동아리 정보", example = "동아리 responseDTO 객체를 반환합니다.")
    private CircleResponseDto circle;

    @ApiModelProperty(value ="동아리 게시판 리스트", example = "동아리의 속한 게시판 목록을 List<BoardOfCircleResponseDto> 객체(리스트)로 반환합니다.")
    private List<BoardOfCircleResponseDto> boardList;

    public static CircleBoardsResponseDto from(
            CircleResponseDto circle,
            List<BoardOfCircleResponseDto> boardList
    ) {
        return CircleBoardsResponseDto.builder()
                .circle(circle)
                .boardList(boardList)
                .build();
    }

    public static CircleBoardsResponseDto toCircleBoardsResponseDto(Circle circle, Long numMember, List<BoardOfCircleResponseDto> boardList) {
        return CircleServiceDtoMapper.INSTANCE.toCircleBoardsResponseDto(CircleResponseDto.toCircleResponseDtoExtended(circle, numMember), boardList);
    }
}
