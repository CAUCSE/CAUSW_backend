package net.causw.app.main.domain.community.board.service.dto.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigDetail;
import net.causw.app.main.domain.user.account.entity.user.User;

@Mapper(componentModel = "spring", uses = BoardConfigAdminMapper.class)
public interface BoardConfigDetailMapper {

	@Mapping(source = "board.id", target = "boardId")
	@Mapping(source = "board.name", target = "name")
	@Mapping(source = "board.description", target = "description")
	@Mapping(source = "boardConfig.anonymous", target = "isAnonymous")
	@Mapping(source = "boardConfig.readScope", target = "readScope")
	@Mapping(source = "boardConfig.writeScope", target = "writeScope")
	@Mapping(source = "boardConfig.notice", target = "isNotice")
	@Mapping(target = "visibility", expression = "java(boardConfig != null && boardConfig.getVisibility() != null ? boardConfig.getVisibility().name() : null)")
	@Mapping(source = "boardConfig.displayOrder", target = "displayOrder")
	@Mapping(source = "admins", target = "admins")
	BoardConfigDetail fromEntity(Board board, BoardConfig boardConfig, List<User> admins);
}
