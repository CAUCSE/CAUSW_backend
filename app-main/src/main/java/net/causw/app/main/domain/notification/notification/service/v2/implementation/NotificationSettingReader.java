package net.causw.app.main.domain.notification.notification.service.v2.implementation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.entity.UserNotificationSetting;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.repository.UserNotificationSettingRepository;
import net.causw.app.main.domain.notification.notification.service.v2.dto.UserNotificationSettingMap;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingReader {

	private final UserNotificationSettingRepository userNotificationSettingRepository;

	/**
	 * DB에서 userId로 설정을 모두 조회하여 Map으로 변환한다.
	 * <br> Map의 key는 UserNotificationSettingKey enum, value는 설정된 boolean 값이다.
	 * <br> DB에 저장된 설정이 없는 경우, UserNotificationSettingKey의 defaultEnabled 값을 사용하여 Map을 완성한다.
	 * @param userId 조회할 유저의 ID
	 * @return UserNotificationSettingMap: userId에 대한 전체 알림 설정 맵 (저장된 값이 없는 키는 enum의 defaultEnabled로 채워짐)
	 */
	public UserNotificationSettingMap findSettingMap(String userId) {
		Map<UserNotificationSettingKey, Boolean> storedMap = userNotificationSettingRepository.findAllByUserId(userId)
			.stream()
			.collect(Collectors.toMap(
				UserNotificationSetting::getSettingKey,
				UserNotificationSetting::isEnabled));

		return UserNotificationSettingMap.ofFull(storedMap);
	}

	/**
	 * DB에서 userId로 설정을 모두 조회하여 List로 반환한다.
	 * @param userId 조회할 유저의 ID
	 * @return List<UserNotificationSetting>: userId에 대한 전체 알림 설정 리스트
	 */
	public List<UserNotificationSetting> findAllByUserId(String userId) {
		return userNotificationSettingRepository.findAllByUserId(userId);
	}
}
