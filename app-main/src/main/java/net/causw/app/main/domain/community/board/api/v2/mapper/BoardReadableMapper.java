package net.causw.app.main.domain.community.board.api.v2.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardReadableItemResponse;
import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardReadableListResponse;
import net.causw.app.main.domain.community.board.service.v2.dto.BoardReadableItemResult;

@Mapper(componentModel = "spring")
public interface BoardReadableMapper {

	BoardReadableItemResponse toReadableItemResponse(BoardReadableItemResult result);

	List<BoardReadableItemResponse> toReadableItemResponseList(List<BoardReadableItemResult> results);

	default BoardReadableListResponse toReadableListResponse(List<BoardReadableItemResult> results) {
		return new BoardReadableListResponse(toReadableItemResponseList(results));
	}
}
