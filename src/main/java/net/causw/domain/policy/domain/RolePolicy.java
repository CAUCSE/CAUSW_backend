package net.causw.domain.policy.domain;

import net.causw.domain.model.enums.user.Role;

import java.util.Map;
import java.util.Set;

public class RolePolicy {
    // Update Role(권한 위임 및 부여 공통)
    public static final Set<Role> ROLES_UPDATABLE_BY_PRESIDENT = Set.of(Role.VICE_PRESIDENT, Role.COUNCIL, Role.COMMON);

    // Delegate Role(권한 위임)
    // 위임 가능한 권한
    public static final Set<Role> DELEGATABLE_ROLES = Set.of(Role.ADMIN, Role.PRESIDENT);

    // Grant Role(권한 부여)
    // 특정 권한(Key)이 부여 가능한 권한(Value)을 설정함.
    public static final Map<Role, Set<Role>> GRANTABLE_ROLES = Map.of(
            Role.ADMIN, Set.of(
                    Role.PRESIDENT,
                    Role.VICE_PRESIDENT,
                    Role.COUNCIL,
                    Role.LEADER_1,
                    Role.LEADER_2,
                    Role.LEADER_3,
                    Role.LEADER_4,
                    Role.LEADER_ALUMNI,
                    Role.COMMON
            ),

            Role.PRESIDENT, Set.of(
                    Role.VICE_PRESIDENT,
                    Role.COUNCIL,
                    Role.LEADER_1,
                    Role.LEADER_2,
                    Role.LEADER_3,
                    Role.LEADER_4,
                    Role.LEADER_ALUMNI,
                    Role.COMMON
            )
    );

    // 특정 권한(Key)이 대리 위임 가능한 권한(Value)을 설정함.
    public static final Map<Role, Set<Role>> PROXY_DELEGATABLE_ROLES = Map.of(
            Role.ADMIN, Set.of(
                    Role.PRESIDENT,
                    Role.VICE_PRESIDENT,
                    Role.COUNCIL,
                    Role.LEADER_1,
                    Role.LEADER_2,
                    Role.LEADER_3,
                    Role.LEADER_4,
                    Role.LEADER_ALUMNI
            ),

            Role.PRESIDENT, Set.of(
                    Role.VICE_PRESIDENT,
                    Role.COUNCIL,
                    Role.LEADER_1,
                    Role.LEADER_2,
                    Role.LEADER_3,
                    Role.LEADER_4,
                    Role.LEADER_ALUMNI
            )
    );
}
