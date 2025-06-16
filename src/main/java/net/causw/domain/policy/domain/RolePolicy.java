package net.causw.domain.policy.domain;

import net.causw.domain.model.enums.user.Role;

import java.util.Map;
import java.util.Set;

public class RolePolicy {
    // Delegate Role(권한 위임)
    // 위임 가능한 권한
    public static final Set<Role> DELEGATABLE_ROLES = Set.of(Role.ADMIN, Role.PRESIDENT);

    public static final Set<Role> ROLES_DELEGATABLE_BY_PRESIDENT = Set.of(Role.VICE_PRESIDENT, Role.COUNCIL, Role.COMMON);

    public static final Set<Role> NON_PROXY_DELEGATABLE_ROLES = Set.of(Role.ADMIN, Role.COMMON, Role.NONE);

    // Grant Role(권한 부여)
    public static final Set<Role> NON_GRANTABLE_ROLES = Set.of(Role.ADMIN, Role.NONE);

    public static final Set<Role> DEFAULT_GRANTOR_ROLES = Set.of(Role.ADMIN, Role.PRESIDENT);

    public static final Map<Role, Set<Role>> GRANTOR_ROLES = Map.of(
            Role.PRESIDENT, Set.of(Role.ADMIN)
    );

    public static Set<Role> getGrantorRoles(Role role) {
        return GRANTOR_ROLES.getOrDefault(role, DEFAULT_GRANTOR_ROLES);
    }
}
