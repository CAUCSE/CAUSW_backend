package net.causw.app.main.domain.community.board.api.v2.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardWritableItemResponse;
import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardWritableListResponse;
import net.causw.app.main.domain.community.board.service.v2.dto.BoardWritableItemResult;

@Mapper(componentModel = "spring")
public interface BoardWritableMapper {
	BoardWritableItemResponse toWritableItemResponse(BoardWritableItemResult result);

	List<BoardWritableItemResponse> toWritableItemResponseList(List<BoardWritableItemResult> results);

	default BoardWritableListResponse toWritableListResponse(List<BoardWritableItemResult> results) {
		return new BoardWritableListResponse(toWritableItemResponseList(results));
	}
}
