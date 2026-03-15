package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserMeResponse;
import net.causw.app.main.domain.user.account.service.dto.result.UserMeResult;

@Mapper(componentModel = "spring")
public interface UserMeMapper {

	UserMeResponse toResponse(UserMeResult result);
}
