package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserSearchListResponse;
import net.causw.app.main.domain.user.account.service.dto.result.UserSearchListResult;

@Mapper(componentModel = "spring")
public interface UserSearchListMapper {

	UserSearchListResponse toResponse(UserSearchListResult userSearchListResult);
}
