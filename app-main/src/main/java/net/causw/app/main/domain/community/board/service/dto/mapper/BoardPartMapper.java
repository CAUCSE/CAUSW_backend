package net.causw.app.main.domain.community.board.service.dto.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.dto.request.BoardPart;

@Mapper(componentModel = "spring")
public interface BoardPartMapper {

	BoardPart fromEntity(Board board);

	default Board toEntity(BoardPart part) {
		return Board.createForV2(part.name(), part.description());
	}
}
