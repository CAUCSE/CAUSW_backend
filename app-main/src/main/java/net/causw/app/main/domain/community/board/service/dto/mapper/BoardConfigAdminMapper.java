package net.causw.app.main.domain.community.board.service.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigAdmin;
import net.causw.app.main.domain.user.account.entity.user.User;

@Mapper(componentModel = "spring")
public interface BoardConfigAdminMapper {

	@Mapping(source = "email", target = "adminEmail")
	@Mapping(source = "name", target = "adminName")
	BoardConfigAdmin fromEntity(User user);
}
