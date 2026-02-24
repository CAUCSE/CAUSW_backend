package net.causw.app.main.domain.notification.notification.service.v2.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.notification.notification.entity.UserNotificationSetting;
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

	/**
	 * 요청에 포함된 키만 upsert한다 (부분 업데이트).
	 */
	public void upsertSettings(String userId, UserNotificationSettingMap settingMap) {
		settingMap.forEach((key, value) ->
			userNotificationSettingRepository
				.findByUserIdAndSettingKey(userId, key)
				.ifPresentOrElse(
					// db에 존재 시 update
					existing -> existing.updateEnabled(value),
					// db에 존재하지 않을시, insert
					() -> userNotificationSettingRepository.save(
						UserNotificationSetting.of(userId, key, value))
				)
		);
	}

	/**
	 * 공식 게시판 구독 상태를 upsert한다.
	 */
	public void upsertBoardSubscribe(User user, Board board, boolean subscribed) {
		userBoardSubscribeWriter.upsertBoardSubscribe(user, board, subscribed);
	}
}
