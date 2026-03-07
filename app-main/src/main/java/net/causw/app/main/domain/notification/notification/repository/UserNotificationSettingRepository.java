package net.causw.app.main.domain.notification.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.notification.notification.entity.UserNotificationSetting;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;

@Repository
public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, String> {

	List<UserNotificationSetting> findAllByUserId(String userId);

	Optional<UserNotificationSetting> findByUserIdAndSettingKey(String userId, UserNotificationSettingKey settingKey);
}
