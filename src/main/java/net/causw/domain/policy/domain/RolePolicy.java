package net.causw.domain.policy.domain;

import net.causw.domain.model.enums.user.Role;

import java.util.Map;
import java.util.Set;

public class RolePolicy {
    // Grant Role(권한 위임)
    public static final Set<Role> GRANTABLE_ROLES = Set.of(Role.ADMIN, Role.PRESIDENT, Role.LEADER_ALUMNI);

    public static final Set<Role> NON_GRANTABLE_ROLES = Set.of(Role.ADMIN, Role.COMMON, Role.NONE);

    public static final Set<Role> ROLES_GRANTABLE_BY_PRESIDENT = Set.of(Role.VICE_PRESIDENT, Role.COUNCIL, Role.COMMON);

    // Update Role(권한 설정)
    public static final Set<Role> NON_UPDATABLE_ROLES = Set.of(Role.ADMIN, Role.NONE);

    public static final Set<Role> DEFAULT_UPDATER_ROLES = Set.of(Role.ADMIN, Role.PRESIDENT);

    public static final Map<Role, Set<Role>> UPDATER_ROLES = Map.of(
            Role.PRESIDENT, Set.of(Role.ADMIN)
    );

    public static Set<Role> getUpdaterRoles(Role role) {
        return UPDATER_ROLES.getOrDefault(role, DEFAULT_UPDATER_ROLES);
    }
}
