package net.causw.app.main.domain.community.board.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardSearchCondition;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;

@Mapper(componentModel = "spring")
public interface BoardSearchConditionMapper {
	BoardQueryCondition toServiceDto(BoardSearchCondition boardSearchCondition);
}
