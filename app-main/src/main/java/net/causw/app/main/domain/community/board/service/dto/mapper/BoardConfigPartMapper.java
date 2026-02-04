package net.causw.app.main.domain.community.board.service.dto.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigPart;

@Mapper(componentModel = "spring")
public interface BoardConfigPartMapper {

	BoardConfigPart fromEntity(BoardConfig boardConfig);

	default BoardConfig toEntity(BoardConfigPart part, String boardId, int displayOrder) {
		return BoardConfig.of(
			boardId,
			part.isAnonymous(),
			part.readScope(),
			part.writeScope(),
			part.isNotice(),
			part.visibility(),
			displayOrder);
	}
}
