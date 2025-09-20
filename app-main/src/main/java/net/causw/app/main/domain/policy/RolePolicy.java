package net.causw.app.main.domain.policy;

import static java.util.Map.*;
import static net.causw.app.main.domain.model.enums.user.Role.*;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import net.causw.app.main.domain.model.enums.user.Role;

public class RolePolicy {

	/**
	 * 단일 보유 권한 정책
	 * - Key: 권한
	 * - Value: 해당 권한을 사용자가 단 하나만 보유할 수 있는지 여부
	 */
	private static final Map<Role, Boolean> ROLE_UNIQUE = Map.ofEntries(
		entry(ADMIN, false),
		entry(PRESIDENT, true),
		entry(VICE_PRESIDENT, true),
		entry(COUNCIL, false),
		entry(LEADER_1, false),
		entry(LEADER_2, false),
		entry(LEADER_3, false),
		entry(LEADER_4, false),
		entry(LEADER_ALUMNI, true),
		entry(ALUMNI_MANAGER, false),
		entry(COMMON, false),
		entry(NONE, false),

		entry(LEADER_CIRCLE, false),
		entry(PROFESSOR, false)
	);

	/**
	 * 권한 우선순위 정책
	 * - Key: 권한
	 * - Value: 권한의 우선순위 (값이 작을수록 높은 우선순위)
	 */
	private static final Map<Role, Integer> ROLE_PRIORITY = Map.ofEntries(
		entry(ADMIN, 0),
		entry(PRESIDENT, 1),
		entry(VICE_PRESIDENT, 2),
		entry(COUNCIL, 3),
		entry(LEADER_1, 4),
		entry(LEADER_2, 4),
		entry(LEADER_3, 4),
		entry(LEADER_4, 4),
		entry(LEADER_ALUMNI, 5),
		entry(ALUMNI_MANAGER, 5),
		entry(COMMON, 99),
		entry(NONE, 100),

		entry(LEADER_CIRCLE, 5),
		entry(PROFESSOR, 6)
	);

	/**
	 * 권한 설정 가능 대상 정책 (부여 및 위임 공통)
	 * - Key: 설정하려는 권한
	 * - Value: 해당 권한을 설정받을 수 있는 수혜자의 권한 목록
	 */
	private static final Map<Role, Set<Role>> ROLES_ASSIGNABLE_FOR = Map.of(
		// 부학생회장과 학생회 권한이 같이 삭제되므로 대상이 일반, 학생회장, 부학생회장 권한까지 설정 가능함.
		PRESIDENT, Set.of(VICE_PRESIDENT, COUNCIL, COMMON),
		// 일반 권한의 경우 모두 권한에 설정 가능함.
		COMMON, EnumSet.allOf(Role.class)
	);

	/**
	 * 권한 위임 정책
	 * - 이 Set에 포함된 권한만 사용자가 다른 사용자에게 위임 가능
	 */
	private static final Set<Role> DELEGATABLE_ROLES = Set.of(ADMIN, PRESIDENT);

	/**
	 * 권한 부여 정책
	 * - Key: 부여자 권한
	 * - Value: 부여자가 수혜자에게 부여 가능한 권한 목록
	 */
	private static final Map<Role, Set<Role>> GRANTABLE_ROLES = Map.of(
		ADMIN, Set.of(
			PRESIDENT,
			VICE_PRESIDENT,
			COUNCIL,
			LEADER_1,
			LEADER_2,
			LEADER_3,
			LEADER_4,
			LEADER_ALUMNI,
			ALUMNI_MANAGER,
			COMMON
		),

		PRESIDENT, Set.of(
			VICE_PRESIDENT,
			COUNCIL,
			LEADER_1,
			LEADER_2,
			LEADER_3,
			LEADER_4,
			LEADER_ALUMNI,
			ALUMNI_MANAGER,
			COMMON
		)
	);

	/**
	 * 대리 위임 정책
	 * - Key: 부여자 권한
	 * - Value: 부여자가 대리로 위임 가능한 권한 목록
	 */
	private static final Map<Role, Set<Role>> PROXY_DELEGATABLE_ROLES = Map.of(
		ADMIN, Set.of(
			PRESIDENT,
			VICE_PRESIDENT,
			COUNCIL,
			LEADER_1,
			LEADER_2,
			LEADER_3,
			LEADER_4,
			LEADER_ALUMNI,
			ALUMNI_MANAGER
		),

		PRESIDENT, Set.of(
			VICE_PRESIDENT,
			COUNCIL,
			LEADER_1,
			LEADER_2,
			LEADER_3,
			LEADER_4,
			LEADER_ALUMNI,
			ALUMNI_MANAGER
		)
	);

	// --- Getter Methods ---

	/**
	 * 단일 보유 권한 여부 반환
	 * - 사용자가 동시에 하나만 가질 수 있는 권한인지 여부
	 *
	 * @param role 확인할 권한
	 * @return true: 단일 보유, false: 복수 보유 가능
	 */
	public static Boolean getRoleUnique(Role role) {
		return ROLE_UNIQUE.get(role);
	}

	/**
	 * 권한 우선순위 반환
	 * - 값이 작을수록 우선순위 높음
	 * - 예: ADMIN(0), PRESIDENT(1), ..., COMMON(99)
	 *
	 * @param role 대상 권한
	 * @return 우선순위 (작을수록 우선)
	 */
	public static Integer getRolePriority(Role role) {
		return ROLE_PRIORITY.get(role);
	}

	/**
	 * 설정 가능 대상 권한 목록 반환
	 * - 특정 권한이 어떤 수혜자의 권한에 부여 가능한지 정의
	 *
	 * @param role 부여할 권한
	 * @return 설정 가능 대상 권한 목록
	 */
	public static Set<Role> getRolesAssignableFor(final Role role) {
		return ROLES_ASSIGNABLE_FOR.getOrDefault(role, Set.of(COMMON));
	}

	/**
	 * 위임 가능한 전체 권한 목록 반환
	 * - 사용자가 다른 사용자에게 위임할 수 있는 권한 집합
	 *
	 * @return 위임 가능 권한 목록
	 */
	public static Set<Role> getDelegatableRoles() {
		return DELEGATABLE_ROLES;
	}

	/**
	 * 권한 부여 가능 목록 반환
	 * - 주어진 권한이 다른 사용자에게 부여 가능한 권한 집합
	 *
	 * @param role 부여자 권한
	 * @return 부여 가능 권한 목록
	 */
	public static Set<Role> getGrantableRoles(final Role role) {
		return GRANTABLE_ROLES.getOrDefault(role, Set.of());
	}

	/**
	 * 대리 위임 가능 목록 반환
	 * - 주어진 권한이 타인을 대신해 위임할 수 있는 권한 집합
	 *
	 * @param role 위임자 권한
	 * @return 대리 위임 가능 권한 목록
	 */
	public static Set<Role> getProxyDelegatableRoles(final Role role) {
		return PROXY_DELEGATABLE_ROLES.getOrDefault(role, Set.of());
	}

	// --- Policy Methods ---

	/**
	 * 부여 가능 여부 판단
	 * - 대상 사용자가 가진 모든 권한이 설정 가능 대상에 포함되는지 검사
	 *
	 * @param assignedRole 부여하려는 권한
	 * @param assigneeRoles 대상 사용자의 권한
	 * @return 부여 가능 여부
	 */
	public static boolean canAssign(Role assignedRole, Set<Role> assigneeRoles) {
		// 부여할 권한은 대상의 모든 권한에 대한 부여 가능 권한을 가지고 있어야 함.
		return RolePolicy.getRolesAssignableFor(assignedRole).containsAll(assigneeRoles);
	}

	/**
	 * 권한 역전 여부 판단
	 * - 부여자보다 수혜자의 우선순위가 더 높으면 권한 역전으로 간주
	 *
	 * @param assignerRoles 부여자의 권한
	 * @param assigneeRoles 수혜자의 권한
	 * @return 권한 역전 여부
	 */
	public static boolean isPrivilegeInverted(Set<Role> assignerRoles, Set<Role> assigneeRoles) {
		// 부여자가 가진 모든 권한 중 최상위 우선순위를 권한을 찾음
		int assignerMinPriority = assignerRoles.stream()
			.mapToInt(RolePolicy::getRolePriority)
			.min()
			.orElse(Integer.MAX_VALUE);

		// 수혜자가 가진 모든 권한 중 최상위 우선순위를 찾음
		int assigneeMinPriority = assigneeRoles.stream()
			.mapToInt(RolePolicy::getRolePriority)
			.min()
			.orElse(Integer.MAX_VALUE);

		// 수혜자가 부여자 보다 낮은 값(높은 우선순위)을 가지면 권한 역전
		return assignerMinPriority > assigneeMinPriority;
	}
}
