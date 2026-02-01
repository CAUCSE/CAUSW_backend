package net.causw.app.main.domain.community.board.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardConfigEditResponse;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigDetail;

@Mapper(componentModel = "spring", uses = BoardConfigAdminResponseMapper.class)
public interface BoardConfigEditResponseMapper {
	BoardConfigEditResponse toResponse(BoardConfigDetail boardConfigDetail);
}
