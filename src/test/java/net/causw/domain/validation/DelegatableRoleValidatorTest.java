package net.causw.domain.validation;

import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.ObjectFixtures;
import net.causw.domain.policy.domain.RolePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static net.causw.domain.model.enums.user.Role.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DelegatableRoleValidatorTest {

    private final User delegator = ObjectFixtures.getUser();
    private final User delegatee = ObjectFixtures.getUser();

    private static final Map<Role, Integer> MOCK_ROLE_PRIORITY = Map.ofEntries(
            entry(ADMIN, 0),
            entry(PRESIDENT, 1),
            entry(VICE_PRESIDENT, 2),
            entry(COUNCIL, 3),
            entry(LEADER_1, 4),
            entry(LEADER_2, 4),
            entry(LEADER_3, 4),
            entry(LEADER_4, 4),
            entry(LEADER_ALUMNI, 5),
            entry(COMMON, 99),
            entry(NONE, 100),

            entry(LEADER_CIRCLE, 5),
            entry(PROFESSOR, 6)
    );

    private static final Map<Role, Set<Role>> MOCK_ROLES_ASSIGNABLE_FOR = Map.of(
            Role.PRESIDENT, Set.of(Role.VICE_PRESIDENT, Role.COUNCIL, Role.COMMON),
            Role.VICE_PRESIDENT, Set.of(Role.PRESIDENT)
    );

    private static final Set<Role> MOCK_DELEGATABLE_ROLES = Set.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT);

    @Test
    @DisplayName("위임 권한이 위임 가능 대상일 경우 성공")
    void a_Success() {
        // given
        Role delegatedRole = Role.ADMIN;
        delegator.setRoles(Set.of(delegatedRole));
        delegatee.setRoles(Set.of(Role.COMMON));

        // when & then
        assertValidatorSuccess(delegatedRole);
    }
    
    @Test
    @DisplayName("위임 권한이 위임 가능 대상이 아닐 경우 실패")
    void a_Failure() {
        // given
        Role delegatedRole = Role.COUNCIL;
        delegator.setRoles(Set.of(delegatedRole));
        delegatee.setRoles(Set.of(Role.COMMON));

        // when & then
        assertValidatorFail(delegatedRole);
    }

    @Test
    @DisplayName("위임자가 위임할 권한일 경우 성공")
    void b_Success() {
        // given
        Role delegatedRole = Role.PRESIDENT;
        delegator.setRoles(Set.of(delegatedRole));
        delegatee.setRoles(Set.of(Role.COMMON));

        // when & then
        assertValidatorSuccess(delegatedRole);
    }
    
    @Test
    @DisplayName("위임자가 위임할 권한이 아닐 경우 실패")
    void b_Failure() {
        // given
        delegator.setRoles(Set.of(Role.PRESIDENT));
        delegatee.setRoles(Set.of(Role.COMMON));

        // when & then
        assertValidatorFail(Role.VICE_PRESIDENT);
    }

    @Test
    @DisplayName("위임할 권한이 피위임자의 모든 권한에 대한 위임 가능 권한을 가지고 있을 경우 성공")
    void c_Success() {
        // given
        Role delegatedRole = Role.PRESIDENT;
        delegator.setRoles(Set.of(delegatedRole));
        delegatee.setRoles(Set.of(Role.COUNCIL));

        // when & then
        assertValidatorSuccess(delegatedRole);
    }
    
    @Test
    @DisplayName("위임할 권한이 피위임자의 모든 권한에 대한 위임 가능 권한을 가지고 있지 않을 경우 실패")
    void c_Failure() {
        // given
        Role delegatedRole = Role.PRESIDENT;
        delegator.setRoles(Set.of(delegatedRole));
        delegatee.setRoles(Set.of(Role.NONE));

        // when & then
        assertValidatorFail(delegatedRole);
    }

    @Test
    @DisplayName("위임자가 피위임자 보다 권한 우선순위가 높은 경우 성공")
    void d_Success() {
        // given
        Role delegatedRole = Role.PRESIDENT;
        delegator.setRoles(Set.of(delegatedRole));
        delegatee.setRoles(Set.of(Role.VICE_PRESIDENT));

        // when & then
        assertValidatorSuccess(delegatedRole);
    }

    @Test
    @DisplayName("피위임자가 위임자 보다 권한 우선순위가 높은 경우 실패")
    void d_Failure() {
        // given
        Role delegatedRole = Role.VICE_PRESIDENT;
        delegator.setRoles(Set.of(delegatedRole));
        delegatee.setRoles(Set.of(Role.PRESIDENT));

        // when & then
        assertValidatorFail(delegatedRole);
    }

    private DelegatableRoleValidator createValidator(Role delegatedRole) {
        return DelegatableRoleValidator.of(delegator.getRoles(), delegatedRole, delegatee.getRoles());
    }

    private void withMockedRolePolicy(Role delegatedRole, Runnable assertions) {
        try (MockedStatic<RolePolicy> rolePolicyMockedStatic = Mockito.mockStatic(RolePolicy.class)) {
            rolePolicyMockedStatic.when(() -> RolePolicy.getRolesAssignableFor(delegatedRole))
                    .thenReturn(MOCK_ROLES_ASSIGNABLE_FOR.getOrDefault(delegatedRole, Set.of(Role.COMMON)));

            rolePolicyMockedStatic.when(RolePolicy::getDelegatableRoles)
                    .thenReturn(MOCK_DELEGATABLE_ROLES);

            Stream.concat(delegator.getRoles().stream(), delegatee.getRoles().stream()).forEach(role ->
                    rolePolicyMockedStatic.when(() -> RolePolicy.getRolePriority(role))
                            .thenReturn(MOCK_ROLE_PRIORITY.get(role)));

            assertions.run();
        }
    }

    private void assertValidatorSuccess(Role delegatedRole) {
        DelegatableRoleValidator validator = createValidator(delegatedRole);
        withMockedRolePolicy(delegatedRole, () ->
                assertThatCode(validator::validate)
                        .doesNotThrowAnyException()
        );
    }

    private void assertValidatorFail(Role delegatedRole) {
        DelegatableRoleValidator validator = createValidator(delegatedRole);
        withMockedRolePolicy(delegatedRole, () ->
                assertThatThrownBy(validator::validate)
                        .isInstanceOf(UnauthorizedException.class)
                        .hasMessageContaining(MessageUtil.GRANT_ROLE_NOT_ALLOWED)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.GRANT_ROLE_NOT_ALLOWED)
        );
    }
}
