package net.causw.app.main.domain.admin.audit.api.v2.mapper;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.audit.api.v2.dto.response.AdminAuditLogResponse;
import net.causw.app.main.domain.admin.audit.api.v2.dto.response.AuditActorResponse;
import net.causw.app.main.domain.admin.audit.api.v2.dto.response.AuditTargetResponse;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogItem;
import net.causw.app.main.domain.user.account.enums.user.UserAdminActionType;

@Component
public class AdminAuditLogMapper {

	private static final String USER_TARGET_TYPE = "USER";

	public AdminAuditLogResponse toResponse(AdminAuditLogItem item) {
		return new AdminAuditLogResponse(
			item.id(),
			item.category(),
			item.actionType().name(),
			item.actionType().getDescription(),
			new AuditActorResponse(item.actorUserId(), item.actorEmail()),
			new AuditTargetResponse(USER_TARGET_TYPE, item.targetId(), item.targetEmail()),
			toSummary(item),
			toMetadata(item),
			item.createdAt());
	}

	private String toSummary(AdminAuditLogItem item) {
		if (item.actionType() == UserAdminActionType.DROP) {
			return item.actorEmail() + " dropped user " + item.targetEmail();
		}
		if (item.actionType() == UserAdminActionType.RESTORE) {
			return item.actorEmail() + " restored user " + item.targetEmail();
		}
		if (item.actionType() == UserAdminActionType.ROLE_CHANGE) {
			return item.actorEmail() + " changed roles for user " + item.targetEmail();
		}
		return item.actorEmail() + " performed " + item.actionType().name() + " on user " + item.targetEmail();
	}

	private Map<String, Object> toMetadata(AdminAuditLogItem item) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("beforeState", item.beforeState() == null ? null : item.beforeState().name());
		metadata.put("afterState", item.afterState() == null ? null : item.afterState().name());
		metadata.put("beforeRoles", item.beforeRoles());
		metadata.put("afterRoles", item.afterRoles());
		metadata.put("reason", item.reason());
		return metadata;
	}
}
