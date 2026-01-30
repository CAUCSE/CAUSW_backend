package net.causw.app.main.domain.community.board.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardConfigUpdateRequest;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigUpdateCommand;

@Mapper(componentModel = "spring")
public interface BoardConfigUpdateRequestMapper {

	BoardConfigUpdateCommand toCommand(BoardConfigUpdateRequest request);
}
