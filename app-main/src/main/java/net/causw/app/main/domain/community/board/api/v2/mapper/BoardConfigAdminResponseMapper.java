package net.causw.app.main.domain.community.board.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardConfigEditResponse.AdminResponse;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigAdmin;

@Mapper(componentModel = "spring")
public interface BoardConfigAdminResponseMapper {

	AdminResponse toResponse(BoardConfigAdmin boardConfigAdmin);
}
