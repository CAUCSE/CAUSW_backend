package net.causw.app.main.domain.notification.notification.service.v2.implementation;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.entity.UserNotificationSetting;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeRepository;
import net.causw.app.main.domain.notification.notification.repository.UserNotificationSettingRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class NotificationSettingWriter {

	private final UserNotificationSettingRepository userNotificationSettingRepository;
	private final UserBoardSubscribeRepository userBoardSubscribeRepository;

	/**
	 * 요청에 포함된 키만 upsert한다 (부분 업데이트).
	 */
	public void upsertSettings(String userId, Map<UserNotificationSettingKey, Boolean> settingMap) {
		for (Map.Entry<UserNotificationSettingKey, Boolean> entry : settingMap.entrySet()) {
			userNotificationSettingRepository
				.findByUserIdAndSettingKey(userId, entry.getKey())
				.ifPresentOrElse(
					existing -> existing.updateEnabled(entry.getValue()),
					() -> userNotificationSettingRepository.save(
						UserNotificationSetting.of(userId, entry.getKey(), entry.getValue()))
				);
		}
	}

	/**
	 * 공식 게시판 구독 상태를 upsert한다.
	 */
	public void upsertBoardSubscribe(User user, Board board, boolean subscribed) {
		userBoardSubscribeRepository.findByUserAndBoard(user, board)
			.ifPresentOrElse(
				existing -> existing.setIsSubscribed(subscribed),
				() -> userBoardSubscribeRepository.save(
					UserBoardSubscribe.of(user, board, subscribed))
			);
	}
}
