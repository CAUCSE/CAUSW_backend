package net.causw.app.main.service.user;

import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.user.UserResponseDto;
import net.causw.app.main.dto.user.UserUpdateRoleRequestDto;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.util.ObjectFixtures;
import net.causw.app.main.domain.policy.RolePolicy;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static net.causw.app.main.domain.model.enums.user.Role.*;
import static net.causw.app.main.domain.model.enums.user.Role.COMMON;
import static net.causw.app.main.domain.policy.RolePolicy.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserRoleServiceTest {

	private static final Map<Role, Boolean> MOCK_ROLE_UNIQUE = Map.ofEntries(
		entry(ADMIN, false),
		entry(PRESIDENT, true),
		entry(VICE_PRESIDENT, true),
		entry(COUNCIL, false),
		entry(LEADER_1, false),
		entry(LEADER_2, false),
		entry(LEADER_3, false),
		entry(LEADER_4, false),
		entry(LEADER_ALUMNI, true),
		entry(COMMON, false),
		entry(NONE, false),

		entry(LEADER_CIRCLE, false),
		entry(PROFESSOR, false)
	);
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
		// 부학생회장과 학생회 권한이 같이 삭제되므로 대상이 일반, 학생회장, 부학생회장 권한까지 설정 가능함.
		PRESIDENT, Set.of(VICE_PRESIDENT, COUNCIL, COMMON),
		// 일반 권한의 경우 모두 권한에 설정 가능함.
		COMMON, EnumSet.allOf(Role.class)
	);
	@InjectMocks
	UserRoleService userRoleService;
	@Mock
	UserRepository userRepository;

	private void mockDefaultRolePolicy(MockedStatic<RolePolicy> rolePolicyMockedStatic,
		Set<Role> assignerRoles, Role assignedRole, Set<Role> assigneeRoles) {
		rolePolicyMockedStatic.when(() -> getRolesAssignableFor(assignedRole))
			.thenReturn(MOCK_ROLES_ASSIGNABLE_FOR.getOrDefault(assignedRole, Set.of(Role.COMMON)));

		rolePolicyMockedStatic.when(() -> getRoleUnique(assignedRole))
			.thenReturn(MOCK_ROLE_UNIQUE.get(assignedRole));

		Stream.concat(assignerRoles.stream(), assigneeRoles.stream()).forEach(role ->
			rolePolicyMockedStatic.when(() -> getRolePriority(role))
				.thenReturn(MOCK_ROLE_PRIORITY.get(role)));

		rolePolicyMockedStatic.when(() -> canAssign(any(), any()))
			.thenCallRealMethod();

		rolePolicyMockedStatic.when(() -> isPrivilegeInverted(any(), any()))
			.thenCallRealMethod();
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
				}
			}

			when(userRepository.findByRoleAndState(role, UserState.ACTIVE)).thenReturn(dummyUsers);

			mockUsers.put(role, dummyUsers);
		}

		return mockUsers;
	}

	@Nested
	class UpdateRoleTest {

		User user = ObjectFixtures.getUser();

		@Test
		@DisplayName("사용자가 하나의 권한만 가지고 있을 경우 성공(겸직 불가)")
		void whenUserHasSingleRole_thenSuccess() {
			// given
			user.setRoles(Set.of(Role.PRESIDENT, Role.COUNCIL));

			// when
			userRoleService.updateRole(user, Role.COMMON);

			// then
			assertThat(user.getRoles()).isEqualTo(Set.of(Role.COMMON));
		}
	}

	@Nested
	class RemoveRoleTest {

		private final User user = ObjectFixtures.getUser();

		private static Set<Role> getAllRolesWithoutNone() {
			Set<Role> roles = EnumSet.allOf(Role.class);
			roles.remove(Role.NONE);
			return roles;
		}

		@ParameterizedTest
		@MethodSource("getAllRolesWithoutNone")
		@DisplayName("하나 남은 권한을 삭제했을 때 COMMON이 부여될 경우 성공")
		void whenLastRoleDeleted_thenAssignCommon(Role role) {
			// given
			user.setRoles(Set.of(role));

			// when
			userRoleService.removeRole(user, role);

			// then
			assertThat(user.getRoles()).isEqualTo(Set.of(Role.COMMON));
		}

		@Test
		@DisplayName("마지막으로 남은 NONE을 삭제했을 때 NONE이 부여될 경우 성공")
		void whenOnlyNoneLeft_thenRemainAsNone() {
			// given
			user.setRoles(Set.of(Role.NONE));

			// when
			userRoleService.removeRole(user, Role.NONE);

			// then
			assertThat(user.getRoles()).isEqualTo(Set.of(Role.NONE));
		}
	}

	@Nested
	class DelegateRoleTest {
		private static final Set<Role> MOCK_DELEGATABLE_ROLES = Set.of(
			Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT, Role.COUNCIL);
		private final User delegator = ObjectFixtures.getUser();
		private final User delegatee = ObjectFixtures.getUser();
		private final String delegateeId = "dummyDelegateeId";

		@BeforeEach
		void setUp() {
			// when
			when(userRepository.findById(delegateeId)).thenReturn(Optional.of(delegatee));
			when(userRepository.save(delegatee)).thenReturn(delegatee);
		}

		@Test
		@DisplayName("학생회장 권한을 위임할 때 학생회장과 부학생회장 그리고 학생회 권한을 가진 사용자가 없을 경우 성공")
		void whenNoOtherExecutives_thenSuccess() {
			// given
			Role delegatedRole = Role.PRESIDENT;
			delegator.setRoles(Set.of(delegatedRole));
			delegatee.setRoles(Set.of(Role.COMMON));

			// when
			Map<Role, List<User>> mockUsers = mockFindByRoleAndState(
				Set.of(Role.PRESIDENT, Role.VICE_PRESIDENT, Role.COUNCIL), delegator, delegatee);
			mockPolicyAndDelegateRole(delegatedRole);

			// then
			mockUsers.values().stream().flatMap(Collection::stream)
				.forEach(user -> {
					if (user != delegatee) {
						assertThat(user.getRoles()).isEqualTo(Set.of(Role.COMMON));
					}
				});
		}

		@Test
		@DisplayName("고유 권한을 위임할 때 해당 권한을 가진 사용자가 없을 경우 성공")
		void whenNoUserHasUniqueRole_thenSuccess() {
			// given
			Role delegatedRole = Role.VICE_PRESIDENT;
			delegator.setRoles(Set.of(delegatedRole));
			delegatee.setRoles(Set.of(Role.COMMON));

			// when
			Map<Role, List<User>> mockUsers = mockFindByRoleAndState(Set.of(delegatedRole), delegator, delegatee);
			mockPolicyAndDelegateRole(delegatedRole);

			// then
			assertThat(getRoleUnique(delegatedRole)).isTrue();
			mockUsers.values().stream().flatMap(Collection::stream)
				.forEach(user -> {
					if (user != delegatee) {
						assertThat(user.getRoles()).isEqualTo(Set.of(Role.COMMON));
					}
				});
		}

		@Test
		@DisplayName("권한을 위임 후 위임자가 일반 권한일 경우 성공")
		void whenDelegationOccurs_thenGrantorBecomesCommon() {
			// given
			Role delegatedRole = Role.COUNCIL;
			delegator.setRoles(Set.of(delegatedRole));
			delegatee.setRoles(Set.of(Role.COMMON));

			// when
			mockPolicyAndDelegateRole(delegatedRole);

			// then
			assertThat(getRoleUnique(delegatedRole)).isFalse();
			assertThat(delegator.getRoles()).isEqualTo(Set.of(Role.COMMON));
		}

		@Test
		@DisplayName("권한을 위임 후 피위임지가 해당 권한일 경우 성공")
		void whenDelegateeAlreadyHasRole_thenSuccess() {
			// given
			Role delegatedRole = Role.ADMIN;
			delegator.setRoles(Set.of(delegatedRole));
			delegatee.setRoles(Set.of(Role.COMMON));

			// when
			UserResponseDto userResponseDto = mockPolicyAndDelegateRole(delegatedRole);

			// then
			assertThat(delegatee.getRoles()).isEqualTo(Set.of(delegatedRole));
			assertThat(userResponseDto.getRoles()).isEqualTo(Set.of(delegatedRole));
		}

		private UserResponseDto mockPolicyAndDelegateRole(Role delegatedRole) {
			UserResponseDto userResponseDto;

			try (MockedStatic<RolePolicy> rolePolicyMockedStatic = Mockito.mockStatic(RolePolicy.class)) {
				rolePolicyMockedStatic.when(RolePolicy::getDelegatableRoles).thenReturn(MOCK_DELEGATABLE_ROLES);
				mockDefaultRolePolicy(rolePolicyMockedStatic, delegator.getRoles(), delegatedRole,
					delegatee.getRoles());

				userResponseDto = userRoleService.delegateRole(
					delegator, delegateeId, new UserUpdateRoleRequestDto(String.valueOf(delegatedRole)));
			}

			return userResponseDto;
		}
	}

	@Nested
	class GrantRoleTest {

		private static final Map<Role, Set<Role>> MOCK_GRANTABLE_ROLES = Map.of(
			Role.ADMIN, Set.of(
				Role.PRESIDENT,
				Role.LEADER_1
			),

			Role.PRESIDENT, Set.of(
				Role.VICE_PRESIDENT
			)
		);
		private static final Map<Role, Set<Role>> MOCK_PROXY_DELEGATABLE_ROLES = Map.of(
			Role.ADMIN, Set.of(
				Role.COUNCIL
			)
		);
		private final User grantor = ObjectFixtures.getUser();
		private final String granteeId = "dummyDelegateeId";
		private final User grantee = ObjectFixtures.getUser();
		private final String delegatorId = "dummyDelegatorId";
		private User delegator;

		private static Set<Role> getGrantableRoles(Role role) {
			return MOCK_GRANTABLE_ROLES.getOrDefault(role, Set.of());
		}

		private static Set<Role> getProxyDelegatableRoles(Role role) {
			return MOCK_PROXY_DELEGATABLE_ROLES.getOrDefault(role, Set.of());
		}

		@BeforeEach
		void setUp() {
			// when
			when(userRepository.findById(granteeId)).thenReturn(Optional.of(grantee));
			when(userRepository.save(grantee)).thenReturn(grantee);
		}

		@Test
		@DisplayName("학생회장 권한을 부여할 때 학생회장과 부학생회장 그리고 학생회 권한을 가진 사용자가 없을 경우 성공")
		void whenNoOtherExecutives_thenSuccess() {
			// given
			grantor.setRoles(Set.of(Role.ADMIN));
			grantee.setRoles(Set.of(Role.COMMON));

			// when
			Map<Role, List<User>> mockUsers = mockFindByRoleAndState(
				Set.of(Role.PRESIDENT, Role.VICE_PRESIDENT, Role.COUNCIL), grantor, grantee);
			mockPolicyAndGrantRole(Role.PRESIDENT);

			// then
			mockUsers.values().stream().flatMap(Collection::stream)
				.forEach(user -> {
					if (user != grantee) {
						assertThat(user.getRoles()).isEqualTo(Set.of(Role.COMMON));
					}
				});
		}

		@Test
		@DisplayName("고유 권한을 부여할 때 해당 권한을 가진 사용자가 없을 경우 성공")
		void whenNoUserHasUniqueRole_thenSuccess() {
			// given
			Role grantedRole = Role.VICE_PRESIDENT;
			grantor.setRoles(Set.of(Role.PRESIDENT));
			grantee.setRoles(Set.of(Role.COMMON));

			// when
			Map<Role, List<User>> mockUsers = mockFindByRoleAndState(Set.of(grantedRole), grantor, grantee);
			mockPolicyAndGrantRole(grantedRole);

			// then
			assertThat(getRoleUnique(grantedRole)).isTrue();
			mockUsers.values().stream().flatMap(Collection::stream)
				.forEach(user -> {
					if (user != grantee) {
						assertThat(user.getRoles()).isEqualTo(Set.of(Role.COMMON));
					}
				});
		}

		@Test
		@DisplayName("위임자가 있을 때 권한을 대리 위임 후 위임자가 일반 권한일 경우 성공")
		void whenProxyDelegation_thenOriginalGrantorBecomesCommon() {
			// given
			Role grantedRole = Role.COUNCIL;
			delegator = ObjectFixtures.getUser();
			grantor.setRoles(Set.of(Role.ADMIN));
			delegator.setRoles(Set.of(grantedRole));
			grantee.setRoles(Set.of(Role.COMMON));

			// when
			mockPolicyAndProxyDelegateRole(grantedRole);

			// then
			assertThat(getRoleUnique(grantedRole)).isFalse();
			assertThat(delegator.getRoles()).isEqualTo(Set.of(Role.COMMON));
		}

		@Test
		@DisplayName("권한을 부여 후 수혜자가 해당 권한일 경우 성공")
		void whenAssigneeAlreadyHasRole_thenSuccess() {
			// given
			Role grantedRole = Role.LEADER_1;
			grantor.setRoles(Set.of(Role.ADMIN));
			grantee.setRoles(Set.of(Role.COMMON));

			// when
			UserResponseDto userResponseDto = mockPolicyAndGrantRole(grantedRole);

			// then
			assertThat(grantee.getRoles()).isEqualTo(Set.of(grantedRole));
			assertThat(userResponseDto.getRoles()).isEqualTo(Set.of(grantedRole));
		}

		private UserResponseDto mockPolicyAndProxyDelegateRole(Role grantedRole) {
			when(userRepository.findById(delegatorId)).thenReturn(Optional.of(delegator));
			return mockPolicyAndGrantRole(delegatorId, grantedRole);
		}

		private UserResponseDto mockPolicyAndGrantRole(Role grantedRole) {
			return mockPolicyAndGrantRole(null, grantedRole);
		}

		private UserResponseDto mockPolicyAndGrantRole(String delegatorId, Role grantedRole) {
			UserResponseDto userResponseDto;

			try (MockedStatic<RolePolicy> rolePolicyMockedStatic = Mockito.mockStatic(RolePolicy.class)) {
				if (delegator == null) {
					grantor.getRoles().forEach(role -> rolePolicyMockedStatic
						.when(() -> RolePolicy.getGrantableRoles(role))
						.thenReturn(getGrantableRoles(role)));
				} else {
					grantor.getRoles().forEach(role -> rolePolicyMockedStatic
						.when(() -> RolePolicy.getProxyDelegatableRoles(role))
						.thenReturn(getProxyDelegatableRoles(role)));
				}

				mockDefaultRolePolicy(rolePolicyMockedStatic, grantor.getRoles(), grantedRole, grantee.getRoles());

				userResponseDto = userRoleService.grantRole(
					grantor, delegatorId, granteeId, new UserUpdateRoleRequestDto(String.valueOf(grantedRole)));
			}

			return userResponseDto;
		}
	}
}
