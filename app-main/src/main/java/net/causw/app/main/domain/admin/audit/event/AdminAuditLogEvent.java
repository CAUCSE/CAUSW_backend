package net.causw.app.main.domain.admin.audit.event;

import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCreateCommand;

public record AdminAuditLogEvent(
	AdminAuditLogCreateCommand command) {
}
