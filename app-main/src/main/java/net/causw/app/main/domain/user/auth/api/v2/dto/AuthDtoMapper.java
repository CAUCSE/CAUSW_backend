package net.causw.app.main.domain.user.auth.api.v2.dto;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;

@Mapper(componentModel = "spring")
public interface AuthDtoMapper {

	AuthResponse toAuthResponse(AuthResult authResult);
}
