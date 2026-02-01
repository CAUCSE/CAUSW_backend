package net.causw.app.main.domain.community.board.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardConfigUpdateRequest;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigPart;
import net.causw.app.main.domain.community.board.service.dto.request.BoardPart;

@Mapper(componentModel = "spring")
public interface BoardConfigUpdateRequestMapper {

	BoardPart toBoardPart(BoardConfigUpdateRequest request);

	BoardConfigPart toBoardConfigPart(BoardConfigUpdateRequest request);
}
