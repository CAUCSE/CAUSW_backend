package net.causw.app.main.domain.community.board.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardAdminListResponse;
import net.causw.app.main.domain.community.board.service.dto.result.BoardListResult;

@Mapper(componentModel = "spring")
public interface BoardAdminListMapper {

	BoardAdminListResponse toResponse(BoardListResult boardListResult);
}
