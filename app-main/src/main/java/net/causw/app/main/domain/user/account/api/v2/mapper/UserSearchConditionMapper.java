package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserSearchCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserQueryCondition;

@Mapper(componentModel = "spring")
public interface UserSearchConditionMapper {

	UserQueryCondition toServiceDto(UserSearchCondition userSearchCondition);
}
