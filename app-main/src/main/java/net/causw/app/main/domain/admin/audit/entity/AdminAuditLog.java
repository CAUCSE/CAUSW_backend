package net.causw.app.main.domain.admin.audit.entity;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_admin_audit_log")
public class AdminAuditLog extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false)
	private AdminAuditLogCategory category;

	@Column(name = "action_type", nullable = false)
	private String actionType;

	@Column(name = "action_description", nullable = false)
	private String actionDescription;

	@Column(name = "actor_user_id", nullable = false)
	private String actorUserId;

	@Column(name = "actor_email", nullable = false)
	private String actorEmail;

	@Column(name = "actor_name")
	private String actorName;

	@Column(name = "actor_student_id")
	private String actorStudentId;

	@Column(name = "target_type", nullable = false)
	private String targetType;

	@Column(name = "target_id", nullable = false)
	private String targetId;

	@Column(name = "target_email")
	private String targetEmail;

	@Column(name = "target_name")
	private String targetName;

	@Column(name = "target_student_id")
	private String targetStudentId;

	@Column(name = "summary", nullable = false)
	private String summary;

	@Column(name = "metadata_json", columnDefinition = "TEXT")
	private String metadataJson;

	public static AdminAuditLog of(
		AdminAuditLogCategory category,
		String actionType,
		String actionDescription,
		String actorUserId,
		String actorEmail,
		String actorName,
		String actorStudentId,
		String targetType,
		String targetId,
		String targetEmail,
		String targetName,
		String targetStudentId,
		String summary,
		String metadataJson) {
		return AdminAuditLog.builder()
			.category(category)
			.actionType(actionType)
			.actionDescription(actionDescription)
			.actorUserId(actorUserId)
			.actorEmail(actorEmail)
			.actorName(actorName)
			.actorStudentId(actorStudentId)
			.targetType(targetType)
			.targetId(targetId)
			.targetEmail(targetEmail)
			.targetName(targetName)
			.targetStudentId(targetStudentId)
			.summary(summary)
			.metadataJson(metadataJson)
			.build();
	}
}
