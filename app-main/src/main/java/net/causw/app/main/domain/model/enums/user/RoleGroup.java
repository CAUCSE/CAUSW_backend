package net.causw.app.main.domain.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Getter
@AllArgsConstructor
public enum RoleGroup {
    EXECUTIVES(Set.of( // 핵심 집행부
            Role.ADMIN,
            Role.PRESIDENT,
            Role.VICE_PRESIDENT
    )),

    EXECUTIVES_AND_PROFESSOR(Set.of( // 집행부 + 교수
            Role.ADMIN,
            Role.PRESIDENT,
            Role.VICE_PRESIDENT,
            Role.PROFESSOR
    )),

    EXECUTIVES_AND_CIRCLE_LEADER(Set.of( // 집행부 + 동아리장
            Role.ADMIN,
            Role.PRESIDENT,
            Role.VICE_PRESIDENT,
            Role.LEADER_CIRCLE
    )),

    CAN_LEAVE(Set.of( // 탈퇴 가능 권한
            Role.COMMON,
            Role.PROFESSOR
    )),

    OPERATIONS_TEAM(Set.of( // 운영진
            Role.ADMIN,
            Role.PRESIDENT,
            Role.VICE_PRESIDENT,
            Role.COUNCIL,
            Role.LEADER_CIRCLE,
            Role.LEADER_1,
            Role.LEADER_2,
            Role.LEADER_3,
            Role.LEADER_4,
            Role.LEADER_ALUMNI
    ));

    private final Set<Role> roles;

    @Component("RoleGroup")
    public static class RoleGroupComponent {
        public static final RoleGroup EXECUTIVES = RoleGroup.EXECUTIVES;
        public static final RoleGroup EXECUTIVES_AND_PROFESSOR = RoleGroup.EXECUTIVES_AND_PROFESSOR;
        public static final RoleGroup EXECUTIVES_AND_CIRCLE_LEADER = RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER;
        public static final RoleGroup CAN_LEAVE = RoleGroup.CAN_LEAVE;
        public static final RoleGroup OPERATIONS_TEAM = RoleGroup.OPERATIONS_TEAM;
    }
}
