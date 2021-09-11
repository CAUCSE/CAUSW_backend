package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Board;
import net.causw.adapter.persistence.Circle;
import net.causw.application.dto.BoardCreateRequestDto;
import net.causw.application.dto.BoardFullDto;
import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.BoardUpdateRequestDto;
import net.causw.application.dto.CircleFullDto;
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
    public Optional<BoardFullDto> findById(String id) {
        return this.boardRepository.findById(id).map(BoardFullDto::from);
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

    @Override
    public Optional<BoardResponseDto> update(String id, BoardUpdateRequestDto boardUpdateRequestDto) {
        return this.boardRepository.findById(id).map(
                srcBoard -> {
                    srcBoard.setName(boardUpdateRequestDto.getName());
                    srcBoard.setDescription(boardUpdateRequestDto.getDescription());
                    srcBoard.setCreateRoles(boardUpdateRequestDto.getCreateRoleList().stream().map(Object::toString).collect(Collectors.joining(",")));
                    srcBoard.setModifyRoles(boardUpdateRequestDto.getModifyRoleList().stream().map(Object::toString).collect(Collectors.joining(",")));
                    srcBoard.setReadRoles(boardUpdateRequestDto.getReadRoleList().stream().map(Object::toString).collect(Collectors.joining(",")));

                    return BoardResponseDto.from(this.boardRepository.save(srcBoard));
                }
        );
    }
}
