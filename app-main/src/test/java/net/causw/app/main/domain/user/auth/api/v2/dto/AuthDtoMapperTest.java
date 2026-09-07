package net.causw.app.main.domain.user.auth.api.v2.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
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

	@Test
	@DisplayName("인증 결과는 생성 시 전달된 역할 목록의 변경에 영향을 받지 않는다")
	void copyRolesInAuthResultCanonicalConstructor() {
		// given
		Set<Role> roles = new HashSet<>(Set.of(Role.COMMON));
		AuthResult result = new AuthResult(
			"access-token", "사용자", "user@example.com", null, "refresh-token", false, true, true, null, roles);

		// when
		roles.add(Role.ADMIN);

		// then
		assertThat(result.roles()).containsExactly(Role.COMMON);
	}

	@Test
	@DisplayName("인증 응답은 생성 시 전달된 역할 목록의 변경에 영향을 받지 않는다")
	void copyRolesInAuthResponseCanonicalConstructor() {
		// given
		Set<Role> roles = new HashSet<>(Set.of(Role.COMMON));
		AuthResponse response = new AuthResponse(
			"access-token", "refresh-token", "사용자", "user@example.com", null, null, null, roles);

		// when
		roles.add(Role.ADMIN);

		// then
		assertThat(response.roles()).containsExactly(Role.COMMON);
	}
}
