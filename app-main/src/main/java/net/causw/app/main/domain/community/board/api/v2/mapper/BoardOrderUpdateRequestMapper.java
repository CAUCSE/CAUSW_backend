package net.causw.app.main.domain.community.board.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardOrderUpdateRequest;
import net.causw.app.main.domain.community.board.service.dto.request.BoardOrderUpdateCommand;

@Mapper(componentModel = "spring")
public interface BoardOrderUpdateRequestMapper {

	BoardOrderUpdateCommand toCommand(BoardOrderUpdateRequest request);
}
