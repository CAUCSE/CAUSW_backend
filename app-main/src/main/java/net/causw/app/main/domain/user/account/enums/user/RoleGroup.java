package net.causw.app.main.domain.user.account.enums.user;

import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleGroup {
	EXECUTIVES(Set.of(Role.ADMIN)),

	EXECUTIVES_AND_PROFESSOR(Set.of(Role.ADMIN)),

	EXECUTIVES_AND_LEADER_ALUMNI(Set.of(Role.ADMIN)),

	EXECUTIVES_AND_CIRCLE_LEADER(Set.of(Role.ADMIN)),

	CAN_LEAVE(Set.of(Role.COMMON)),

	OPERATIONS_TEAM(Set.of(Role.ADMIN));

	private final Set<Role> roles;

	@Component("RoleGroup")
	public static class RoleGroupComponent {
		public static final RoleGroup EXECUTIVES = RoleGroup.EXECUTIVES;

		public static final RoleGroup EXECUTIVES_AND_LEADER_ALUMNI = RoleGroup.EXECUTIVES_AND_LEADER_ALUMNI;
		public static final RoleGroup EXECUTIVES_AND_PROFESSOR = RoleGroup.EXECUTIVES_AND_PROFESSOR;
		public static final RoleGroup EXECUTIVES_AND_CIRCLE_LEADER = RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER;
		public static final RoleGroup CAN_LEAVE = RoleGroup.CAN_LEAVE;
		public static final RoleGroup OPERATIONS_TEAM = RoleGroup.OPERATIONS_TEAM;
	}
}
