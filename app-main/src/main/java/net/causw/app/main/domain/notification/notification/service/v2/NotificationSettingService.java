package net.causw.app.main.domain.notification.notification.service.v2;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.service.v2.dto.NotificationSettingResult;
import net.causw.app.main.domain.notification.notification.service.v2.dto.UpdateNotificationSettingCommand;
import net.causw.app.main.domain.notification.notification.service.v2.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.v2.implementation.NotificationSettingWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

	private final NotificationSettingReader notificationSettingReader;
	private final NotificationSettingWriter notificationSettingWriter;
	private final UserReader userReader;

	/**
	 * 개인별 고정 토글 + 공식계정 게시판 구독 설정을 한 번에 반환한다.
	 * DB에 row가 없으면 enum의 defaultEnabled를 적용한다.
	 */
	public NotificationSettingResult getAllSettings(String userId) {
		User user = userReader.findUserById(userId);
		Map<UserNotificationSettingKey, Boolean> settingMap = notificationSettingReader.findSettingMap(userId);

		NotificationSettingResult.CommunitySettings community = new NotificationSettingResult.CommunitySettings(
			settingMap.get(UserNotificationSettingKey.COMMUNITY_LIKE_ON_MY_POST),
			settingMap.get(UserNotificationSettingKey.COMMUNITY_COMMENT_ON_MY_POST),
			settingMap.get(UserNotificationSettingKey.COMMUNITY_REPLY_ON_MY_COMMENT)
		);

		NotificationSettingResult.CeremonySettings ceremony = new NotificationSettingResult.CeremonySettings(
			settingMap.get(UserNotificationSettingKey.CEREMONY_NOTIFICATION_ENABLED)
		);

		NotificationSettingResult.ServiceSettings service = new NotificationSettingResult.ServiceSettings(
			settingMap.get(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED)
		);

		List<NotificationSettingResult.OfficialBoardSetting> officialBoards =
			notificationSettingReader.findOfficialBoardSettings(user);

		return new NotificationSettingResult(community, ceremony, service, officialBoards);
	}

	/**
	 * 요청에 포함된 개인별 고정 토글만 upsert한다 (부분 업데이트).
	 */
	@Transactional
	public void updateUserSettings(String userId, UpdateNotificationSettingCommand command) {
		Map<UserNotificationSettingKey, Boolean> settingMap = command.toSettingMap();
		if (settingMap.isEmpty()) {
			return;
		}
		notificationSettingWriter.upsertSettings(userId, settingMap);
	}

	/**
	 * 공식계정 게시판 구독 상태를 upsert한다.
	 * boardId가 is_notice=true인 게시판인지 검증한다.
	 */
	@Transactional
	public void updateOfficialBoardSubscribe(String userId, String boardId, boolean subscribed) {
		User user = userReader.findUserById(userId);
		Board board = notificationSettingReader.findNoticeBoardOrThrow(boardId);
		notificationSettingWriter.upsertBoardSubscribe(user, board, subscribed);
	}
}
