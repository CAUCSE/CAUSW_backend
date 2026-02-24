package net.causw.app.main.domain.notification.notification.service.v2.dto;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;

/**
 * 유저별 알림 설정 키-값 쌍을 담는 값 객체.
 * Map 제네릭의 Boolean wrapper 노출을 내부로 캡슐화하고,
 * get() 시 null 대신 enum의 defaultEnabled를 반환한다.
 */
public class UserNotificationSettingMap {

	private final Map<UserNotificationSettingKey, Boolean> map;

	private UserNotificationSettingMap(Map<UserNotificationSettingKey, Boolean> map) {
		this.map = map;
	}

	/**
	 * DB 조회 결과(storedMap)로 전체 설정 맵을 생성한다.
	 * 저장된 값이 없는 키는 enum의 defaultEnabled로 채운다.
	 */
	public static UserNotificationSettingMap ofFull(Map<UserNotificationSettingKey, Boolean> storedMap) {
		// UserNotificationSettingKey 에 대한 enumMap 생성
		var result = new EnumMap<UserNotificationSettingKey, Boolean>(UserNotificationSettingKey.class);
		// 조회 결과가 없는 경우, UserNotificationSettingKey의 기본 설정 사용
		for (UserNotificationSettingKey key : UserNotificationSettingKey.values()) {
			result.put(key, storedMap.getOrDefault(key, key.isDefaultEnabled()));
		}

		return new UserNotificationSettingMap(result);
	}

	/**
	 * 부분 업데이트용 설정 맵을 생성한다. null 필드는 포함되지 않는다.
	 */
	public static UserNotificationSettingMap ofPartial(Map<UserNotificationSettingKey, Boolean> partialMap) {
		return new UserNotificationSettingMap(new EnumMap<>(partialMap));
	}

	/**
	 *
	 * @param key 조회 설정 key
	 * @return 조회 설정 on off (유저가 따로 설정하지 않았을 시, 기본 value 반환)
	 */
	public boolean get(UserNotificationSettingKey key) {
		Boolean value = map.get(key);
		return value != null ? value : key.isDefaultEnabled();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Map의 forEach를 그대로 위임(delegate)한 메서드
	 * @param action 실행 함수
	 */
	public void forEach(BiConsumer<UserNotificationSettingKey, Boolean> action) {
		map.forEach(action);
	}
}
