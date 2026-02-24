package net.causw.app.main.domain.notification.notification.service.v2.implementation;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.notification.notification.entity.UserNotificationSetting;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.repository.UserNotificationSettingRepository;
import net.causw.app.main.domain.notification.notification.service.v2.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class NotificationSettingWriter {

	private final UserNotificationSettingRepository userNotificationSettingRepository;
	private final UserBoardSubscribeWriter userBoardSubscribeWriter;
	private final NotificationSettingReader notificationSettingReader;

	/**
	 * 요청에 포함된 키만 upsert한다 (부분 업데이트).
	 */
	public void upsertSettings(String userId, UserNotificationSettingMap settingMap) {
		Map<UserNotificationSettingKey, UserNotificationSetting> storedMap = notificationSettingReader
			.findAllByUserId(userId)
			.stream()
			.collect(Collectors.toMap(
				UserNotificationSetting::getSettingKey,
				Function.identity()));

		settingMap.forEach(
			(key, value) -> {
				// DB에 저장된 기존 설정 조회
				UserNotificationSetting existing = storedMap.get(key);

				if(existing == null) {
					// 기존에 DB에 저장된 설정이 없는 경우 새로 생성하여 저장, 있는 경우 기존 엔티티 업데이트
					userNotificationSettingRepository.save(
							UserNotificationSetting.of(userId, key, value));
				} else {
					// 기존 엔티티 업데이트 (변경된 값이 있는 경우에만 업데이트)
					existing.updateEnabled(value);
				}
			});
	}

	/**
	 * 공식 게시판 구독 상태를 upsert한다.
	 */
	public void upsertBoardSubscribe(User user, Board board, boolean subscribed) {
		userBoardSubscribeWriter.upsertBoardSubscribe(user, board, subscribed);
	}
}
