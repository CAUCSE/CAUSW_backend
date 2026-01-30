package net.causw.app.main.domain.community.board.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardConfigListResponse;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigListResult;

@Mapper(componentModel = "spring")
public interface BoardAdminListMapper {

	BoardConfigListResponse toResponse(BoardConfigListResult boardListResult);
}
