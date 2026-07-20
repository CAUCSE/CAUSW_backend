package net.causw.app.main.domain.user.account.service.implementation;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.audit.event.AdminAuditLogEventPublisher;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserAdminActionType;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class UserAdminActionLogWriter {
	private static final String META_BEFORE_STATE = "이전상태";
	private static final String META_AFTER_STATE = "이후상태";
	private static final String META_BEFORE_ROLES = "이전역할";
	private static final String META_AFTER_ROLES = "이후역할";
	private static final String META_REASON = "사유";

	private final AdminAuditLogEventPublisher adminAuditLogEventPublisher;

	public void logDrop(User adminUser, User targetUser, UserState beforeState, Set<Role> beforeRoles, String reason) {
		writeUserActionLog(
			adminUser,
			targetUser,
			UserAdminActionType.DROP,
			beforeState,
			targetUser.getState(),
			serializeRoles(beforeRoles),
			serializeRoles(targetUser.getRoles()),
			reason);
	}

	public void logRestore(User adminUser, User targetUser, UserState beforeState, Set<Role> beforeRoles) {
		writeUserActionLog(
			adminUser,
			targetUser,
			UserAdminActionType.RESTORE,
			beforeState,
			targetUser.getState(),
			serializeRoles(beforeRoles),
			serializeRoles(targetUser.getRoles()),
			null);
	}

	public void logRoleChange(User adminUser, User targetUser, Set<Role> beforeRoles, Set<Role> afterRoles) {
		writeUserActionLog(
			adminUser,
			targetUser,
			UserAdminActionType.ROLE_CHANGE,
			targetUser.getState(),
			targetUser.getState(),
			serializeRoles(beforeRoles),
			serializeRoles(afterRoles),
			null);
	}

	private void writeUserActionLog(
		User adminUser,
		User targetUser,
		UserAdminActionType actionType,
		UserState beforeState,
		UserState afterState,
		String beforeRoles,
		String afterRoles,
		String reason) {
		adminAuditLogEventPublisher.publishUserAction(
			adminUser,
			targetUser,
			actionType.name(),
			actionType.getDescription(),
			summary(adminUser, targetUser, actionType),
			metadata(beforeState, afterState, beforeRoles, afterRoles, reason));
	}

	private String summary(User adminUser, User targetUser, UserAdminActionType actionType) {
		return switch (actionType) {
			case DROP -> "%s dropped user %s".formatted(adminUser.getEmail(), targetUser.getEmail());
			case RESTORE -> "%s restored user %s".formatted(adminUser.getEmail(), targetUser.getEmail());
			case ROLE_CHANGE -> "%s changed roles for user %s".formatted(adminUser.getEmail(), targetUser.getEmail());
		};
	}

	private Map<String, Object> metadata(
		UserState beforeState,
		UserState afterState,
		String beforeRoles,
		String afterRoles,
		String reason) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put(META_BEFORE_STATE, beforeState == null ? null : beforeState.name());
		metadata.put(META_AFTER_STATE, afterState == null ? null : afterState.name());
		metadata.put(META_BEFORE_ROLES, beforeRoles);
		metadata.put(META_AFTER_ROLES, afterRoles);
		metadata.put(META_REASON, reason);
		return metadata;
	}

	private String serializeRoles(Set<Role> roles) {
		return roles.stream()
			.map(Enum::name)
			.sorted(Comparator.naturalOrder())
			.collect(Collectors.joining(","));
	}
}
