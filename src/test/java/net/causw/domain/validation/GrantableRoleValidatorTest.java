package net.causw.domain.validation;

import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.ObjectFixtures;
import net.causw.domain.policy.domain.RolePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static net.causw.domain.model.enums.user.Role.*;
import static net.causw.domain.policy.domain.RolePolicy.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class GrantableRoleValidatorTest {

    private final User grantor = ObjectFixtures.getUser();
    private User delegator;
    private final User grantee = ObjectFixtures.getUser();

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
            Role.COMMON, EnumSet.allOf(Role.class)
    );

    private static final Map<Role, Set<Role>> MOCK_GRANTABLE_ROLES = Map.of(
            Role.ADMIN, Set.of(
                    Role.PRESIDENT,
                    LEADER_ALUMNI,
                    Role.COMMON
            ),

            Role.PRESIDENT, Set.of(
                    Role.COMMON
            )
    );

    private static final Map<Role, Set<Role>> MOCK_PROXY_DELEGATABLE_ROLES = Map.of(
            Role.ADMIN, Set.of(
                    Role.PRESIDENT
            ),

            Role.PRESIDENT, Set.of(
                    Role.VICE_PRESIDENT
            )
    );

    // 부여
    @Test
    @DisplayName("위임자가 없을 때 부여자가 부여할 권한에 대한 부여 가능 권한을 가지고 있을 경우 성공")
    void whenNoDelegatorAndGrantorCanGrantRole_thenSuccess() {
        // given
        grantor.setRoles(Set.of(Role.ADMIN));
        grantee.setRoles(Set.of(Role.LEADER_1));

        // when & then
        assertThat(delegator).isNull();
        assertValidatorSuccess(Role.COMMON);
    }

    @Test
    @DisplayName("위임자가 없을 때 부여자가 부여할 권한에 대한 부여 가능 권한을 가지고 있지 않을 경우 실패")
    void whenNoDelegatorAndGrantorCannotGrantRole_thenFail() {
        // given
        grantor.setRoles(Set.of(Role.ADMIN));
        grantee.setRoles(Set.of(Role.LEADER_1));

        // when & then
        assertThat(delegator).isNull();
        assertValidatorFail(Role.NONE);
    }

    // 대리 위임
    @Test
    @DisplayName("위임자가 있을 때 위임자가 대리 위임할 권한일 경우 성공")
    void whenDelegatorCanProxyGrantRole_thenSuccess() {
        // given
        delegator = ObjectFixtures.getUser();
        grantor.setRoles(Set.of(Role.PRESIDENT));
        delegator.setRoles(Set.of(Role.VICE_PRESIDENT));
        grantee.setRoles(Set.of(Role.COMMON));

        // when & then
        assertThat(delegator).isNotNull();
        assertValidatorSuccess(Role.VICE_PRESIDENT);
    }

    @Test
    @DisplayName("위임자가 있을 때 위임자가 대리 위임할 권한이 아닐 경우 실패")
    void whenDelegatorCannotProxyGrantRole_thenFail() {
        // given
        delegator = ObjectFixtures.getUser();
        grantor.setRoles(Set.of(Role.PRESIDENT));
        delegator.setRoles(Set.of(Role.COUNCIL));
        grantee.setRoles(Set.of(Role.COMMON));

        // when & then
        assertThat(delegator).isNotNull();
        assertValidatorFail(Role.VICE_PRESIDENT);
    }

    @Test
    @DisplayName("위임자가 있을 때 부여자가 대리 위임할 권한에 대한 대리 위임 가능 권한을 가지고 있을 경우 성공")
    void whenGrantorCanDelegateGrantableRole_thenSuccess() {
        // given
        delegator = ObjectFixtures.getUser();
        grantor.setRoles(Set.of(Role.ADMIN));
        delegator.setRoles(Set.of(Role.PRESIDENT));
        grantee.setRoles(Set.of(Role.COMMON));

        // when & then
        assertThat(delegator).isNotNull();
        assertValidatorSuccess(Role.PRESIDENT);
    }

    @Test
    @DisplayName("위임자가 있을 때 부여자가 대리 위임할 권한에 대한 대리 위임 가능 권한을 가지고 있지 않을 경우 실패")
    void whenGrantorCannotDelegateGrantableRole_thenFail() {
        // given
        delegator = ObjectFixtures.getUser();
        grantor.setRoles(Set.of(Role.PRESIDENT));
        delegator.setRoles(Set.of(Role.PRESIDENT));
        grantee.setRoles(Set.of(Role.COMMON));

        // when & then
        assertThat(delegator).isNotNull();
        assertValidatorFail(Role.PRESIDENT);
    }

    // 공통
    @Test
    @DisplayName("부여자가 수혜자 보다 권한 우선순위가 높은 경우 성공")
    void whenGrantorHasHigherPriorityThanGrantee_thenSuccess() {
        // given
        grantor.setRoles(Set.of(Role.ADMIN));
        grantee.setRoles(Set.of(Role.PRESIDENT));

        // when & then
        assertThat(delegator).isNull();
        assertValidatorSuccess(Role.COMMON);
    }

    @Test
    @DisplayName("수혜자가 부여자 보다 권한 우선순위가 높은 경우 실패")
    void whenGranteeHasHigherPriorityThanGrantor_thenFail() {
        // given
        grantor.setRoles(Set.of(Role.PRESIDENT));
        grantee.setRoles(Set.of(Role.ADMIN));

        // when & then
        assertThat(delegator).isNull();
        assertValidatorFail(Role.COMMON);
    }

    @Test
    @DisplayName("부여할 권한이 수혜자의 모든 권한에 대한 부여 가능 권한을 가지고 있을 경우 성공")
    void whenGrantedRoleCoversAllGranteeRoles_thenSuccess() {
        // given
        grantor.setRoles(Set.of(Role.ADMIN));
        grantee.setRoles(Set.of(Role.COUNCIL));

        // when & then
        assertValidatorSuccess(Role.PRESIDENT);
    }

    @Test
    @DisplayName("부여할 권한이 수혜자의 모든 권한에 대한 부여 가능 권한을 가지고 있지 않을 경우 실패")
    void whenGrantedRoleDoesNotCoverAllGranteeRoles_thenFail() {
        // given
        grantor.setRoles(Set.of(Role.ADMIN));
        grantee.setRoles(Set.of(Role.NONE));

        // when & then
        assertValidatorFail(Role.PRESIDENT);
    }

    // 동문회장
    @Test
    @DisplayName("동문회장 부여 시 수혜자가 졸업생일 경우 성공")
    void whenGrantingAlumniLeaderToGraduate_thenSuccess() {
        // given
        grantor.setRoles(Set.of(Role.ADMIN));
        grantee.setRoles(Set.of(Role.COMMON));
        grantee.setAcademicStatus(AcademicStatus.GRADUATED);

        // when & then
        assertValidatorSuccess(Role.LEADER_ALUMNI);
    }

    @Test
    @DisplayName("동문회장 부여 시 수혜자가 졸업생이 아닐 경우 실패")
    void whenGrantingAlumniLeaderToNonGraduate_thenFail() {
        // given
        grantor.setRoles(Set.of(Role.ADMIN));
        grantee.setRoles(Set.of(Role.COMMON));
        grantee.setAcademicStatus(AcademicStatus.ENROLLED);

        // when & then
        assertValidatorFail(Role.LEADER_ALUMNI);
    }

    private GrantableRoleValidator createGrantableValidator(Role grantedRole) {
        return GrantableRoleValidator.of(grantor.getRoles(), delegator, grantedRole, grantee);
    }

    private void withMockedRolePolicyForGrantable(Role grantedRole, Runnable assertions) {
        try (MockedStatic<RolePolicy> rolePolicyMockedStatic = Mockito.mockStatic(RolePolicy.class)) {
            grantor.getRoles().forEach(role -> {
                if (delegator == null) {
                    rolePolicyMockedStatic.when(() -> getGrantableRoles(role))
                            .thenReturn(MOCK_GRANTABLE_ROLES.getOrDefault(role, Set.of()));
                }
                else {
                    rolePolicyMockedStatic.when(() -> getProxyDelegatableRoles(role))
                            .thenReturn(MOCK_PROXY_DELEGATABLE_ROLES.getOrDefault(role, Set.of()));
                }
            });

            rolePolicyMockedStatic.when(() -> getRolesAssignableFor(grantedRole))
                    .thenReturn(MOCK_ROLES_ASSIGNABLE_FOR.getOrDefault(grantedRole, Set.of(Role.COMMON)));

            Stream.concat(grantor.getRoles().stream(), grantee.getRoles().stream()).forEach(role ->
                    rolePolicyMockedStatic.when(() -> getRolePriority(role))
                            .thenReturn(MOCK_ROLE_PRIORITY.get(role)));

            rolePolicyMockedStatic.when(() -> canAssign(any(), any()))
                    .thenCallRealMethod();

            rolePolicyMockedStatic.when(() -> isPrivilegeInverted(any(), any()))
                    .thenCallRealMethod();

            assertions.run();
        }
    }

    private void assertValidatorSuccess(Role grantedRole) {
        GrantableRoleValidator validator = createGrantableValidator(grantedRole);
        withMockedRolePolicyForGrantable(grantedRole, () ->
                assertThatCode(validator::validate)
                        .doesNotThrowAnyException()
        );
    }

    private void assertValidatorFail(Role grantedRole) {
        GrantableRoleValidator validator = createGrantableValidator(grantedRole);
        withMockedRolePolicyForGrantable(grantedRole, () ->
                assertThatThrownBy(validator::validate)
                        .isInstanceOf(UnauthorizedException.class)
                        .hasMessageContaining(MessageUtil.GRANT_ROLE_NOT_ALLOWED)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.ASSIGN_ROLE_NOT_ALLOWED)
        );
    }
}
