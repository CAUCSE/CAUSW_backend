package net.causw.app.main.domain.community.board.service.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigSummary;

@Mapper(componentModel = "spring")
public interface BoardConfigSummaryMapper {

	@Mapping(source = "no", target = "no")
	@Mapping(source = "board.id", target = "boardId")
	@Mapping(source = "board.name", target = "name")
	@Mapping(source = "board.description", target = "description")
	@Mapping(source = "boardConfig.anonymous", target = "isAnonymous")
	@Mapping(target = "readScope", expression = "java(boardConfig != null && boardConfig.getReadScope() != null ? boardConfig.getReadScope().name() : null)")
	@Mapping(target = "writeScope", expression = "java(boardConfig != null && boardConfig.getWriteScope() != null ? boardConfig.getWriteScope().name() : null)")
	@Mapping(source = "boardConfig.notice", target = "isNotice")
	@Mapping(target = "visibility", expression = "java(boardConfig != null && boardConfig.getVisibility() != null ? boardConfig.getVisibility().name() : null)")
	@Mapping(source = "boardConfig.displayOrder", target = "displayOrder")
	BoardConfigSummary fromEntity(Long no, Board board, BoardConfig boardConfig);
}
