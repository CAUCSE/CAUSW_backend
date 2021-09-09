package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Board;
import net.causw.adapter.persistence.Circle;
import net.causw.application.dto.BoardCreateRequestDto;
import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.CircleFullDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.application.spi.BoardPort;
import net.causw.adapter.persistence.BoardRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BoardPortImpl implements BoardPort {
    private final BoardRepository boardRepository;

    public BoardPortImpl(BoardRepository boardRepository) { this.boardRepository = boardRepository; }

    @Override
    public BoardResponseDto findById(String id) {
        return BoardResponseDto.from(this.boardRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid board id"
                )
        ));
    }

    @Override
    public BoardResponseDto create(BoardCreateRequestDto boardCreateRequestDto, Optional<CircleFullDto> circleFullDto) {
        Circle circle = circleFullDto.map(Circle::from).orElseGet(() -> { return null; });

        return BoardResponseDto.from(this.boardRepository.save(Board.of(
                boardCreateRequestDto.getName(),
                boardCreateRequestDto.getDescription(),
                boardCreateRequestDto.getCreateRoleList().stream().map(Object::toString).collect(Collectors.joining(",")),
                boardCreateRequestDto.getModifyRoleList().stream().map(Object::toString).collect(Collectors.joining(",")),
                boardCreateRequestDto.getReadRoleList().stream().map(Object::toString).collect(Collectors.joining(",")),
                false,
                circle
        )));
    }
}
