package net.causw.app.main.domain.notification.notification.entity;

import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.shared.entity.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "tb_user_notification_setting")
public class UserNotificationSetting extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "setting_key", nullable = false, length = 100)
	private UserNotificationSettingKey settingKey;

	@Column(name = "enabled", nullable = false)
	private boolean enabled;

	public void updateEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public static UserNotificationSetting of(
		String userId,
		UserNotificationSettingKey settingKey,
		boolean enabled) {
		return UserNotificationSetting.builder()
			.userId(userId)
			.settingKey(settingKey)
			.enabled(enabled)
			.build();
	}
}
