package net.causw.app.main.domain.user.account.service.implementation;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdminActionLog;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserAdminActionType;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.UserAdminActionLogRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class UserAdminActionLogWriter {

	private final UserAdminActionLogRepository userAdminActionLogRepository;

	public void logDrop(User adminUser, User targetUser, UserState beforeState, Set<Role> beforeRoles, String reason) {
		userAdminActionLogRepository.save(UserAdminActionLog.of(
			adminUser.getId(),
			adminUser.getEmail(),
			targetUser.getId(),
			targetUser.getEmail(),
			UserAdminActionType.DROP,
			beforeState,
			targetUser.getState(),
			serializeRoles(beforeRoles),
			serializeRoles(targetUser.getRoles()),
			reason));
	}

	public void logRestore(User adminUser, User targetUser, UserState beforeState, Set<Role> beforeRoles) {
		userAdminActionLogRepository.save(UserAdminActionLog.of(
			adminUser.getId(),
			adminUser.getEmail(),
			targetUser.getId(),
			targetUser.getEmail(),
			UserAdminActionType.RESTORE,
			beforeState,
			targetUser.getState(),
			serializeRoles(beforeRoles),
			serializeRoles(targetUser.getRoles()),
			null));
	}

	public void logRoleChange(User adminUser, User targetUser, Set<Role> beforeRoles, Set<Role> afterRoles) {
		userAdminActionLogRepository.save(UserAdminActionLog.of(
			adminUser.getId(),
			adminUser.getEmail(),
			targetUser.getId(),
			targetUser.getEmail(),
			UserAdminActionType.ROLE_CHANGE,
			targetUser.getState(),
			targetUser.getState(),
			serializeRoles(beforeRoles),
			serializeRoles(afterRoles),
			null));
	}

	private String serializeRoles(Set<Role> roles) {
		return roles.stream()
			.map(Enum::name)
			.sorted(Comparator.naturalOrder())
			.collect(Collectors.joining(","));
	}
}
