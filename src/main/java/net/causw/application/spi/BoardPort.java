package net.causw.application.spi;

import net.causw.application.dto.BoardCreateRequestDto;
import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.CircleFullDto;

import java.util.Optional;

public interface BoardPort {
    BoardResponseDto findById(String id);
    BoardResponseDto create(BoardCreateRequestDto boardCreateRequestDto, Optional<CircleFullDto> circleFullDto);
}
