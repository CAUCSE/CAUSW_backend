package net.causw.application.user;

import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.user.UserUpdateRoleRequestDto;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.ObjectFixtures;
import net.causw.domain.policy.domain.RolePolicy;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserRoleServiceTest {

    @InjectMocks
    UserRoleService userRoleService;

    @Mock
    UserRepository userRepository;

    private User delegator;

    @Nested
    class DelegateRoleTest {

        private final User delegatee = ObjectFixtures.getUser();
        private final String delegateeId = "dummyDelegateeId";

        @BeforeEach
        void setUp() {
            // given
            delegator = ObjectFixtures.getUser();

            //when
            when(userRepository.findById(delegateeId)).thenReturn(Optional.of(delegatee));
        }

        @ParameterizedTest
        @MethodSource("getNonDelegatableRoles")
        @DisplayName("위임 권한이 위임 가능 대상이 아닐 경우 실패")
        void a_Failure(Role role) {
            // given
            delegator.setRoles(Set.of(role));
            delegatee.setRoles(Set.of(Role.COMMON));

            // when & then
            assertValidatorFail(delegatee, role);
        }

        @Test
        @DisplayName("위임자가 위임할 권한이 아닐 경우 실패")
        void b_Failure() {
            // given
            delegator.setRoles(Set.of(Role.COMMON));
            delegatee.setRoles(Set.of(Role.COMMON));

            // when & then
            assertValidatorFail(delegator, RolePolicy.DELEGATABLE_ROLES.iterator().next());
        }

        @ParameterizedTest
        @MethodSource("getDelegatableRolesWithoutEdge")
        @DisplayName("피위임자가 특수 권한일 경우 실패(특수 조건을 가진 권한 제외)")
        void c_Failure2(Role role) {
            for (Role specialRole : getSpecialRoles()) {
                // given
                delegator.setRoles(Set.of(role));
                delegatee.setRoles(Set.of(specialRole));

                // when & then
                assertValidatorFail(delegator, role);
            }
        }

        @ParameterizedTest
        @MethodSource("getDelegatableRolesWithoutEdge")
        @DisplayName("피위임자가 일반 권한일 경우 성공(특수 조건을 가진 권한 제외)")
        void a_Success1(Role role) {
            // given
            delegator.setRoles(Set.of(role));
            delegatee.setRoles(Set.of(Role.COMMON));

            // when & then
            assertServiceSuccess(delegator, role);
        }

        @Disabled("현재 특수 조건을 가지지 않는 고유 권한을 위임하는 경우가 없어 실행 안함, 추후 필요 시 활성화")
        @ParameterizedTest
        @MethodSource("getDelegatableAndUniqueRolesWithoutEdge")
        @DisplayName("고유 권한 위임 후 해당 권한을 가진 사용자가 없을 경우 성공(특수 조건을 가진 권한 제외)")
        void a_Success2(Role role) {
            // given
            delegator.setRoles(Set.of(role));
            delegatee.setRoles(Set.of(Role.COMMON));

            // when
            Map<Role, List<User>> mockUsers = mockFindByRoleAndState(Set.of(role), delegator, delegatee);
            userRoleService.delegateRole(delegator, delegateeId, new UserUpdateRoleRequestDto(String.valueOf(role)));

            // then
            mockUsers.get(role).forEach(user -> assertThat(user.getRoles()).isEqualTo(Set.of(Role.COMMON)));
        }

        @ParameterizedTest
        @MethodSource("getRolesDelegatableByPresident")
        @DisplayName("위임자가 학생회장일 때 피위임자가 부학생회장과 학생회 또는 일반 권한일 경우 성공")
        void b_Success1(Role role) {
            // given
            delegator.setRoles(Set.of(Role.PRESIDENT));
            delegatee.setRoles(Set.of(role));

            // when & then
            mockFindByRoleAndState(Set.of(Role.PRESIDENT, Role.VICE_PRESIDENT, Role.COUNCIL), delegator, delegatee);
            assertServiceSuccess(delegator, Role.PRESIDENT);
        }

        @Test
        @DisplayName("위임자가 학생회장일 때 학생회장과 부학생회장 그리고 학생회 권한을 가진 사용자가 없을 경우 성공")
        void b_Success2() {
            // given
            delegator.setRoles(Set.of(Role.PRESIDENT));
            delegatee.setRoles(Set.of(Role.COUNCIL));

            // when
            Map<Role, List<User>> mockUsers = mockFindByRoleAndState(
                    Set.of(Role.PRESIDENT, Role.VICE_PRESIDENT, Role.COUNCIL)
                    , delegator, delegatee);

            userRoleService.delegateRole(delegator, delegateeId, new UserUpdateRoleRequestDto(String.valueOf(Role.PRESIDENT)));

            // then
            mockUsers.values().stream().flatMap(Collection::stream)
                    .forEach(user -> {
                        if (user == delegatee) {
                            assertThat(delegatee.getRoles()).isEqualTo(Set.of(Role.PRESIDENT));
                        }
                        else {
                            assertThat(user.getRoles()).isEqualTo(Set.of(Role.COMMON));
                        }
                    });
        }

        // private methods
        private static Set<Role> getNonDelegatableRoles() {
            return EnumSet.allOf(Role.class).stream()
                    .filter(role -> !RolePolicy.DELEGATABLE_ROLES.contains(role))
                    .collect(Collectors.toSet());
        }

        private static Set<Role> getSpecialRoles() {
            return EnumSet.allOf(Role.class).stream()
                    .filter(role -> !role.equals(Role.COMMON))
                    .collect(Collectors.toSet());
        }

        private static Set<Role> getDelegatableRolesWithoutEdge() {
            return RolePolicy.DELEGATABLE_ROLES.stream()
                    .filter(role -> !role.equals(Role.PRESIDENT))
                    .collect(Collectors.toSet());
        }

        private static Set<Role> getDelegatableAndUniqueRolesWithoutEdge() {
            return RolePolicy.DELEGATABLE_ROLES.stream()
                    .filter(role -> !role.equals(Role.PRESIDENT))
                    .filter(Role::isUnique)
                    .collect(Collectors.toSet());
        }

        private static Set<Role> getRolesDelegatableByPresident() {
            return RolePolicy.ROLES_UPDATABLE_BY_PRESIDENT;
        }

        private void assertServiceSuccess(User delegator, Role targetRole) {
            UserUpdateRoleRequestDto userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(String.valueOf(targetRole));
            userRoleService.delegateRole(delegator, delegateeId, userUpdateRoleRequestDto);
            assertThat(delegator.getRoles()).isEqualTo(Set.of(Role.COMMON));
            assertThat(delegatee.getRoles()).isEqualTo(Set.of(targetRole));
        }

        private void assertValidatorFail(User delegator, Role targetRole) {
            UserUpdateRoleRequestDto userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(String.valueOf(targetRole));
            assertThatThrownBy(() -> userRoleService.delegateRole(delegator, delegateeId, userUpdateRoleRequestDto))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining(MessageUtil.GRANT_ROLE_NOT_ALLOWED)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.GRANT_ROLE_NOT_ALLOWED);
        }
    }

    private Map<Role, List<User>> mockFindByRoleAndState(Set<Role> roles, User... users) {
        Map<Role, List<User>> mockUsers = new HashMap<>();

        for (Role role : roles) {
            List<User> dummyUsers = IntStream.range(0, 3)
                    .mapToObj(i -> {
                        User user = ObjectFixtures.getUser();
                        user.setRoles(Set.of(role));
                        return user;
                    })
                    .collect(Collectors.toCollection(ArrayList::new));

            for (User user : users) {
                if (user != null && user.getRoles().contains(role)) {
                    dummyUsers.add(user);
                    if (delegator.equals(user)) {
                        System.out.println("delegator 드감");
                    }
                }
            }

            when(userRepository.findByRoleAndState(role, UserState.ACTIVE)).thenReturn(dummyUsers);

            mockUsers.put(role, dummyUsers);
        }

        return mockUsers;
    }
}
