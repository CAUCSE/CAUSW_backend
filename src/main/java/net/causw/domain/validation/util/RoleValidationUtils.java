package net.causw.domain.validation.util;

import net.causw.domain.model.enums.user.Role;
import net.causw.domain.policy.domain.RolePolicy;

import java.util.Set;

public class RoleValidationUtils {

    public static boolean canAssign(Role assignedRole, Set<Role> assigneeRoles) {
        // 부여할 권한은 대상의 모든 권한에 대한 부여 가능 권한을 가지고 있어야 함.
        return RolePolicy.getRolesAssignableFor(assignedRole).containsAll(assigneeRoles);
    }

    public static boolean isPrivilegeInverted(Set<Role> assignerRoles, Set<Role> assigneeRoles) {
        // 부여자가 가진 모든 역할 중 최상위 우선순위를 권한을 찾음
        int assignerMinPriority = assignerRoles.stream()
                .mapToInt(RolePolicy::getRolePriority)
                .min()
                .orElse(Integer.MAX_VALUE);

        // 수혜자가 가진 모든 역할 중 최상위 우선순위를 찾음
        int assigneeMinPriority = assigneeRoles.stream()
                .mapToInt(RolePolicy::getRolePriority)
                .min()
                .orElse(Integer.MAX_VALUE);

        // 수혜자가 부여자 보다 낮은 값(높은 우선순위)을 가지면 권한 역전
        return assignerMinPriority > assigneeMinPriority;
    }
}
