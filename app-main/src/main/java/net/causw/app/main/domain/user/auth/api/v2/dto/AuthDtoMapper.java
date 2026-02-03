package net.causw.app.main.domain.user.auth.api.v2.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;

@Mapper(componentModel = "spring")
public interface AuthDtoMapper {

	@Mapping(source = "token", target = "accessToken")
	@Mapping(source = "imageUrl", target = "profileImgUrl")
	AuthResponse toAuthResponse(User user, String token, String imageUrl);
}
