package net.causw.application.spi;

import net.causw.application.dto.BoardCreateRequestDto;
import net.causw.application.dto.BoardFullDto;
import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.BoardUpdateRequestDto;
import net.causw.application.dto.CircleFullDto;

import java.util.Optional;

public interface BoardPort {
    Optional<BoardFullDto> findById(String id);
    BoardResponseDto create(BoardCreateRequestDto boardCreateRequestDto, Optional<CircleFullDto> circleFullDto);
    Optional<BoardResponseDto> update(String id, BoardUpdateRequestDto boardUpdateRequestDto);
}
