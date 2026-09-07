package net.causw.app.main.domain.user.auth.api.v2.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;

class AuthDtoMapperTest {

	private final AuthDtoMapper mapper = Mappers.getMapper(AuthDtoMapper.class);

	@Test
	@DisplayName("인증 응답에 로그인 사용자의 모든 역할을 반환한다")
	void includeRolesInAuthResponse() {
		// given
		AuthResult result = AuthResult.of(
			"access-token", "관리자", "admin@example.com", null, "refresh-token", false, true, true, null,
			Set.of(Role.COMMON, Role.ADMIN));

		// when
		AuthResponse response = mapper.toAuthResponse(result);

		// then
		assertThat(response.roles()).containsExactlyInAnyOrder(Role.COMMON, Role.ADMIN);
	}
}
