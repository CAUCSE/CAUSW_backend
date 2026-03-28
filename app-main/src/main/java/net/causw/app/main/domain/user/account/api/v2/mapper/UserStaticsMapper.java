package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserDailyCountResponse;

@Mapper(componentModel = "spring")
public interface UserStaticsMapper {
	UserDailyCountResponse toDailyCountResponse(UserDailyCountResponse result);
}
