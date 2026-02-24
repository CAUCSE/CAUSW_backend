package net.causw.app.main.domain.notification.notification.service.v2;

import net.causw.app.main.domain.notification.notification.service.v2.dto.OfficialBoardSetting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.notification.notification.service.v2.dto.NotificationSettingResult;
import net.causw.app.main.domain.notification.notification.service.v2.dto.UpdateNotificationSettingCommand;
import net.causw.app.main.domain.notification.notification.service.v2.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.v2.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.v2.implementation.NotificationSettingWriter;
import net.causw.app.main.domain.notification.notification.service.v2.implementation.OfficialBoardSettingReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

	private final NotificationSettingReader notificationSettingReader;
	private final NotificationSettingWriter notificationSettingWriter;
	private final OfficialBoardSettingReader officialBoardSettingReader;
	private final UserReader userReader;

	/**
	 * 개인별 고정 토글 + 공식계정 게시판 구독 설정을 한 번에 반환한다.
	 * DB에 row가 없으면 enum의 defaultEnabled를 적용한다.
	 */
	public NotificationSettingResult getAllSettings(String userId) {
		User user = userReader.findUserById(userId);
		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(userId);

		// 공지사항 게시판 리스트와 유저의 구독 상태를 조회한다.
		List<OfficialBoardSetting> officialBoardSettings = officialBoardSettingReader.findAll(user);

		return NotificationSettingResult.from(settingMap, officialBoardSettings);
	}

	/**
	 * 요청에 포함된 개인별 고정 토글만 upsert한다 (부분 업데이트).
	 */
	@Transactional
	public void updateUserSettings(String userId, UpdateNotificationSettingCommand command) {
		UserNotificationSettingMap settingMap = command.toSettingMap();
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
		Board board = officialBoardSettingReader.findNoticeBoardOrThrow(boardId);
		notificationSettingWriter.upsertBoardSubscribe(user, board, subscribed);
	}
}
