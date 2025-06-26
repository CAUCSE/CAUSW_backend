package net.causw.domain.policy.domain;

import net.causw.domain.model.enums.user.Role;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;
import static net.causw.domain.model.enums.user.Role.*;

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
     * - Key: 부여자 역할
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
                    COMMON
            )
    );

    /**
     * 대리 위임 정책
     * - Key: 부여자 역할
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
                    LEADER_ALUMNI
            ),

            PRESIDENT, Set.of(
                    VICE_PRESIDENT,
                    COUNCIL,
                    LEADER_1,
                    LEADER_2,
                    LEADER_3,
                    LEADER_4,
                    LEADER_ALUMNI
            )
    );

    // --- Getter Methods ---
    public static Boolean getRoleUnique(Role role) {
        return ROLE_UNIQUE.get(role);
    }

    public static Integer getRolePriority(Role role) {
        return ROLE_PRIORITY.get(role);
    }

    public static Set<Role> getRolesAssignableFor(final Role role) {
        return ROLES_ASSIGNABLE_FOR.getOrDefault(role, Set.of(COMMON));
    }

    public static Set<Role> getDelegatableRoles() {
        return DELEGATABLE_ROLES;
    }

    public static Set<Role> getGrantableRoles(final Role role) {
        return GRANTABLE_ROLES.getOrDefault(role, Set.of());
    }

    public static Set<Role> getProxyDelegatableRoles(final Role role) {
        return PROXY_DELEGATABLE_ROLES.getOrDefault(role, Set.of());
    }
}
