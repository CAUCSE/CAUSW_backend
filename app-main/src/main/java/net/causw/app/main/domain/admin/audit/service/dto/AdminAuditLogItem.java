package net.causw.app.main.domain.admin.audit.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.user.account.enums.user.UserAdminActionType;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record AdminAuditLogItem(
	String id,
	AdminAuditLogCategory category,
	String actorUserId,
	String actorEmail,
	String targetId,
	String targetEmail,
	UserAdminActionType actionType,
	UserState beforeState,
	UserState afterState,
	String beforeRoles,
	String afterRoles,
	String reason,
	LocalDateTime createdAt) {
}
