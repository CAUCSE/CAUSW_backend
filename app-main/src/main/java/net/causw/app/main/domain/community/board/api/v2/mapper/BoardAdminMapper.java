package net.causw.app.main.domain.community.board.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardConfigUpdateRequest;
import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardCreateRequest;
import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardConfigEditResponse;
import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardConfigListResponse;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigPart;
import net.causw.app.main.domain.community.board.service.dto.request.BoardPart;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigDetail;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigListResult;

@Mapper(componentModel = "spring")
public interface BoardAdminMapper {

	BoardConfigListResponse toListResponse(BoardConfigListResult boardListResult);

	BoardConfigEditResponse toEditResponse(BoardConfigDetail boardConfigDetail);

	BoardPart toBoardPart(BoardCreateRequest request);

	BoardConfigPart toBoardConfigPart(BoardCreateRequest request);

	BoardPart toBoardPart(BoardConfigUpdateRequest request);

	BoardConfigPart toBoardConfigPart(BoardConfigUpdateRequest request);
}
