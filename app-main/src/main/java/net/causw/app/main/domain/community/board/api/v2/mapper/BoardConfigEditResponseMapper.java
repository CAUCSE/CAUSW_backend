package net.causw.app.main.domain.community.board.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardConfigEditResponse;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigEditResult;

@Mapper(componentModel = "spring")
public interface BoardConfigEditResponseMapper {
	BoardConfigEditResponse toResponse(BoardConfigEditResult boardConfigEditResult);
}
