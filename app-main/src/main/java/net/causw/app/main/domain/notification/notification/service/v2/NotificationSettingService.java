package net.causw.app.main.domain.notification.notification.service.v2;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.notification.notification.service.v2.dto.NotificationSettingResult;
import net.causw.app.main.domain.notification.notification.service.v2.dto.OfficialBoardSetting;
import net.causw.app.main.domain.notification.notification.service.v2.dto.UpdateNotificationSettingCommand;
import net.causw.app.main.domain.notification.notification.service.v2.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.v2.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.v2.implementation.NotificationSettingWriter;
import net.causw.app.main.domain.notification.notification.service.v2.implementation.UserBoardSubscribeReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

	private final NotificationSettingReader notificationSettingReader;
	private final NotificationSettingWriter notificationSettingWriter;
	private final BoardReader boardReader;
	private final UserBoardSubscribeReader userBoardSubscribeReader;
	private final UserReader userReader;
	private final UserValidator userValidator;

	/**
	 * 개인별 고정 토글 + 공식계정 게시판 구독 설정을 한 번에 반환한다.
	 * DB에 row가 없으면 enum의 defaultEnabled를 적용한다.
	 */
	public NotificationSettingResult getAllSettings(String userId) {
		User user = userReader.findUserByIdNotDeleted(userId);
		userValidator.validateUser(user);
		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(userId);

		List<Board> boards = boardReader.findAccessibleNoticeBoards(user.getAcademicStatus());
		List<OfficialBoardSetting> officialBoardSettings = getOfficialBoardSettings(user, boards);

		return NotificationSettingResult.from(settingMap, officialBoardSettings);
	}

	/**
	 * 공식계정 게시판 설정을 반환한다.
	 * @param user 조회 대상 유저
	 * @param boards 조회 대상 게시판 목록 (유저의 학적 상태에 맞는 삭제되지 않은 공지사항 게시판)
	 * @return 공식계정 게시판 설정 목록 (각 게시판에 대해, 유저가 구독 중인지 여부 포함)
	 */
	private List<OfficialBoardSetting> getOfficialBoardSettings(User user, List<Board> boards) {
		if (boards.isEmpty()) {
			return List.of();
		}
		Set<String> subscribedBoardIds = userBoardSubscribeReader.findSubscribedBoardIds(user, boards);

		return boards.stream()
			.map(b -> new OfficialBoardSetting(b.getId(), b.getName(), subscribedBoardIds.contains(b.getId())))
			.toList();
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
		User user = userReader.findUserByIdNotDeleted(userId);
		Board board = boardReader.getNoticeBoardOrThrow(boardId);
		notificationSettingWriter.upsertBoardSubscribe(user, board, subscribed);
	}
}
