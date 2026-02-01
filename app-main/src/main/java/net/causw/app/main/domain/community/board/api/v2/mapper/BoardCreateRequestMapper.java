package net.causw.app.main.domain.community.board.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardCreateRequest;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigPart;
import net.causw.app.main.domain.community.board.service.dto.request.BoardPart;

@Mapper(componentModel = "spring")
public interface BoardCreateRequestMapper {

	BoardPart toBoardPart(BoardCreateRequest request);

	BoardConfigPart toBoardConfigPart(BoardCreateRequest request);
}
