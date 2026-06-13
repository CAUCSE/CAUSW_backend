package net.causw.app.main.domain.admin.audit.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;

public record AdminAuditLogCondition(
	LocalDateTime from,
	LocalDateTime to,
	AdminAuditLogCategory category,
	String actionType,
	String keyword) {
}
