package net.causw.app.main.domain.policy;

import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.policy.RolePolicy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class RolePolicyTest {

	@ParameterizedTest
	@EnumSource(Role.class)
	@DisplayName("단일 보유 권한 정책이 정의되어 있을 경우 성공")
	public void getRoleUnique_whenRoleIsChecked_thenReturnsUnique(Role role) {
		assertThat(RolePolicy.getRoleUnique(role)).isNotNull();
	}

	@ParameterizedTest
	@EnumSource(Role.class)
	@DisplayName("권한 우선순위 정책이 정의되어 있을 경우 성공")
	public void getRolePriority_whenRoleIsChecked_thenReturnsPriority(Role role) {
		assertThat(RolePolicy.getRolePriority(role)).isNotNull();
	}
}
