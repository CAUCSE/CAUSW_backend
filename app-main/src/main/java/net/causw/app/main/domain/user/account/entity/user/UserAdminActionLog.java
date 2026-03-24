package net.causw.app.main.domain.user.account.entity.user;

import net.causw.app.main.domain.user.account.enums.user.UserAdminActionType;
import net.causw.app.main.domain.user.account.enums.user.UserState;
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
@Table(name = "tb_user_admin_action_log")
public class UserAdminActionLog extends BaseEntity {
	@Column(name = "admin_user_id", nullable = false)
	private String adminUserId;

	@Column(name = "admin_user_email", nullable = false)
	private String adminUserEmail;

	@Column(name = "target_user_id", nullable = false)
	private String targetUserId;

	@Column(name = "target_user_email", nullable = false)
	private String targetUserEmail;

	@Enumerated(EnumType.STRING)
	@Column(name = "action_type", nullable = false)
	private UserAdminActionType actionType;

	@Enumerated(EnumType.STRING)
	@Column(name = "before_state")
	private UserState beforeState;

	@Enumerated(EnumType.STRING)
	@Column(name = "after_state")
	private UserState afterState;

	@Column(name = "before_roles")
	private String beforeRoles;

	@Column(name = "after_roles")
	private String afterRoles;

	@Column(name = "reason")
	private String reason;

	public static UserAdminActionLog of(
		String adminUserId,
		String adminUserEmail,
		String targetUserId,
		String targetUserEmail,
		UserAdminActionType actionType,
		UserState beforeState,
		UserState afterState,
		String beforeRoles,
		String afterRoles,
		String reason) {
		return UserAdminActionLog.builder()
			.adminUserId(adminUserId)
			.adminUserEmail(adminUserEmail)
			.targetUserId(targetUserId)
			.targetUserEmail(targetUserEmail)
			.actionType(actionType)
			.beforeState(beforeState)
			.afterState(afterState)
			.beforeRoles(beforeRoles)
			.afterRoles(afterRoles)
			.reason(reason)
			.build();
	}
}
