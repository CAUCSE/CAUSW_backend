package net.causw.domain.policy;

import net.causw.domain.model.enums.user.Role;
import net.causw.domain.policy.domain.RolePolicy;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@ExtendWith(MockitoExtension.class)
public class RolePolicyTest {

    @ParameterizedTest
    @EnumSource(Role.class)
    public void a_Success(Role role) {
        assertThat(RolePolicy.getRoleUnique(role)).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    public void b_Success(Role role) {
        assertThat(RolePolicy.getRolePriority(role)).isNotNull();
    }
}
