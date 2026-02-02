package net.causw.app.main.domain.user.auth.api.v2.dto;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;

@Mapper(componentModel = "spring")
public interface AuthDtoMapper {

	AuthResponse toAuthResponse(User user, String token, String imageUrl);
}
