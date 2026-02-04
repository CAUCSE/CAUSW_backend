package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserDetailResponse;
import net.causw.app.main.domain.user.account.service.dto.response.UserDetailItem;

@Mapper(componentModel = "spring")
public interface UserDetailMapper {

	UserDetailResponse toResponse(UserDetailItem item);
}
