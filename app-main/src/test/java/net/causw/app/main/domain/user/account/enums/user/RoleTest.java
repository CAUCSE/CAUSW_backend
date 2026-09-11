package net.causw.app.main.domain.user.account.enums.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import net.causw.global.exception.BadRequestException;

class RoleTest {

	@ParameterizedTest
	@ValueSource(strings = {
		"PRESIDENT",
		"VICE_PRESIDENT",
		"COUNCIL",
		"LEADER_1",
		"LEADER_2",
		"LEADER_3",
		"LEADER_4",
		"LEADER_ALUMNI",
		"ALUMNI_MANAGER",
		"LEADER_CIRCLE",
		"PROFESSOR"
	})
	@DisplayName("제거된 역할은 더 이상 지원하지 않는다")
	void of_whenRemovedRoleIsGiven_thenThrowsBadRequestException(String removedRole) {
		// when & then
		assertThatThrownBy(() -> Role.of(removedRole)).isInstanceOf(BadRequestException.class);
	}
}
