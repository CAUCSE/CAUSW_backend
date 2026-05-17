package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.account.api.v2.dto.response.SocialAccountsResponse;
import net.causw.app.main.domain.user.account.service.dto.result.SocialAccountsResult;

@Mapper(componentModel = "spring")
public interface SocialAccountsMapper {

	SocialAccountsResponse toResponse(SocialAccountsResult result);
}
