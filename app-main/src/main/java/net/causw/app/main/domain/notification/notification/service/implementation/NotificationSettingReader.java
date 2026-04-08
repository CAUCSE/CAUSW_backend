package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.entity.UserNotificationSetting;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.repository.UserNotificationSettingQueryRepository;
import net.causw.app.main.domain.notification.notification.repository.UserNotificationSettingRepository;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingReader {

	private final UserNotificationSettingRepository userNotificationSettingRepository;
	private final UserNotificationSettingQueryRepository userNotificationSettingQueryRepository;

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

	/**
	 * 알림 설정을 고려하여 발송 대상 유저를 단일 쿼리로 조회한다.
	 * <br> admissionYears가 비어있으면 전체 활성 유저 대상
	 * @param admissionYears 대상 입학년도 목록 (비어있으면 전체)
	 * @param key            판별할 알림 설정 키
	 * @return 발송 대상 User 목록
	 */
	public List<User> findCeremonyNotificationTargets(List<Integer> admissionYears, UserNotificationSettingKey key) {
		return userNotificationSettingQueryRepository.findCeremonyNotificationTargets(admissionYears, key);
	}

	/**
	 * 여러 유저의 설정을 한 번에 조회하여 userId → UserNotificationSettingMap 으로 반환한다.
	 * DB에 저장된 설정이 없는 유저는 enum의 defaultEnabled 값으로 채워진 맵을 반환한다.
	 * @param userIds 조회할 유저 ID 목록
	 * @return Map<userId, UserNotificationSettingMap>
	 */
	public Map<String, UserNotificationSettingMap> findSettingMapByUserIds(List<String> userIds) {
		Map<String, Map<UserNotificationSettingKey, Boolean>> storedByUser = userNotificationSettingRepository
			.findAllByUserIdIn(userIds).stream()
			.collect(Collectors.groupingBy(
				UserNotificationSetting::getUserId,
				Collectors.toMap(
					UserNotificationSetting::getSettingKey,
					UserNotificationSetting::isEnabled)));

		return userIds.stream()
			.collect(Collectors.toMap(
				userId -> userId,
				userId -> UserNotificationSettingMap.ofFull(storedByUser.getOrDefault(userId, Map.of()))));
	}
}
