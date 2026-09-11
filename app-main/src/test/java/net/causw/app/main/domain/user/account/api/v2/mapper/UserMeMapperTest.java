package net.causw.app.main.domain.user.account.api.v2.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserMeResponse;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.service.dto.result.UserMeResult;

class UserMeMapperTest {

	private final UserMeMapper mapper = Mappers.getMapper(UserMeMapper.class);

	@Test
	@DisplayName("내 정보 응답에 사용자의 모든 역할을 반환한다")
	void includeRolesInUserMeResponse() {
		// given
		UserMeResult result = new UserMeResult(
			"user-id", "user@example.com", "사용자", "닉네임", null, 2020, null, null,
			Set.of(Role.COMMON, Role.ADMIN));

		// when
		UserMeResponse response = mapper.toResponse(result);

		// then
		assertThat(response.roles()).containsExactlyInAnyOrder(Role.COMMON, Role.ADMIN);
	}

	@Test
	@DisplayName("내 정보 응답은 생성 시 전달된 역할 목록의 변경에 영향을 받지 않는다")
	void copyRolesInUserMeResponseCanonicalConstructor() {
		// given
		Set<Role> roles = new HashSet<>(Set.of(Role.COMMON));
		UserMeResponse response = new UserMeResponse(
			"user-id", "user@example.com", "사용자", "닉네임", null, 2020, null, null, roles);

		// when
		roles.add(Role.ADMIN);

		// then
		assertThat(response.roles()).containsExactly(Role.COMMON);
	}
}
