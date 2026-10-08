package net.causw.app.main.domain.notification.notification.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardGroup;
import net.causw.app.main.domain.community.board.service.implementation.BoardAccessManager;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.enums.PostCategory;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.service.dto.BoardSubscribeSetting;
import net.causw.app.main.domain.notification.notification.service.dto.NotificationSettingResult;
import net.causw.app.main.domain.notification.notification.service.dto.UpdateNotificationSettingCommand;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingWriter;
import net.causw.app.main.domain.notification.notification.service.implementation.UserBoardSubscribeReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.shared.exception.errorcode.NotificationSettingErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

	private final NotificationSettingReader notificationSettingReader;
	private final NotificationSettingWriter notificationSettingWriter;
	private final BoardReader boardReader;
	private final BoardAccessManager boardAccessManager;
	private final BoardConfigReader boardConfigReader;
	private final UserBoardSubscribeReader userBoardSubscribeReader;
	private final UserReader userReader;
	private final UserValidator userValidator;

	/**
	 * 개인별 고정 토글 + 소식(채널·성격) 설정 + 소통 게시판 구독 설정을 한 번에 반환한다.
	 * DB에 row가 없으면 기본값을 적용한다. (개인별 토글: enum의 defaultEnabled, 소식 게시판: ON, 소통 게시판: OFF)
	 */
	public NotificationSettingResult getAllSettings(String userId) {
		User user = userReader.findUserByIdNotDeleted(userId);
		userValidator.validateUser(user);
		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(userId);

		List<Board> newsBoards = boardAccessManager.getReadableBoards(user, BoardGroup.NOTICE);
		List<Board> communityBoards = excludeSystemNoticeBoards(
			boardAccessManager.getReadableBoards(user, BoardGroup.COMMUNITY));

		return NotificationSettingResult.of(
			settingMap,
			getBoardSubscribeSettings(user, newsBoards, true),
			getBoardSubscribeSettings(user, communityBoards, false));
	}

	/**
	 * 요청에 포함된 개인별 고정 토글만 upsert한다 (부분 업데이트).
	 */
	@Transactional
	public void updateUserSettings(String userId, UpdateNotificationSettingCommand command) {
		validateNewsCategories(command.newsCategories());
		UserNotificationSettingMap settingMap = command.toSettingMap();
		if (settingMap.isEmpty()) {
			return;
		}
		notificationSettingWriter.upsertSettings(userId, settingMap);
	}

	/**
	 * 공식계정 게시판 구독 상태를 upsert한다.
	 * boardId가 is_notice=true인 게시판인지 검증한다.
	 * 기존 앱 호환을 위해 유지하며, 신규 클라이언트는 {@link #updateBoardSubscribe}를 사용한다.
	 */
	@Transactional
	public void updateOfficialBoardSubscribe(String userId, String boardId, boolean subscribed) {
		User user = userReader.findUserByIdNotDeleted(userId);
		Board board = boardReader.getNoticeBoardOrThrow(boardId);
		boardAccessManager.validateCanRead(user, board);
		notificationSettingWriter.upsertBoardSubscribe(user, board, subscribed);
	}

	/**
	 * 소식·소통 게시판 구독 상태를 upsert한다.
	 * 시스템 공지 게시판은 서비스 공지 알림 설정으로 관리하므로 구독할 수 없다.
	 */
	@Transactional
	public void updateBoardSubscribe(String userId, String boardId, boolean subscribed) {
		User user = userReader.findUserByIdNotDeleted(userId);
		Board board = boardReader.getById(boardId);
		BoardConfig boardConfig = boardConfigReader.getByBoardId(boardId);
		if (boardConfig.isSystemNotice()) {
			throw NotificationSettingErrorCode.BOARD_NOT_SUBSCRIBABLE.toBaseException();
		}
		boardAccessManager.validateCanRead(user, board);
		notificationSettingWriter.upsertBoardSubscribe(user, board, subscribed);
	}

	/**
	 * 소식 성격별 알림은 사용자 설정 대상 성격만 수정할 수 있다. (ETC 등은 불가)
	 */
	private void validateNewsCategories(Map<PostCategory, Boolean> newsCategories) {
		if (newsCategories == null) {
			return;
		}
		boolean hasNotConfigurable = newsCategories.keySet().stream()
			.anyMatch(category -> UserNotificationSettingKey.fromNewsCategory(category).isEmpty());
		if (hasNotConfigurable) {
			throw NotificationSettingErrorCode.NEWS_CATEGORY_NOT_CONFIGURABLE.toBaseException();
		}
	}

	/**
	 * 게시판별 구독 설정을 반환한다.
	 * @param user 조회 대상 유저
	 * @param boards 조회 대상 게시판 목록 (유저가 읽을 수 있는 삭제되지 않은 게시판)
	 * @param defaultSubscribed 설정 row가 없는 경우의 기본 구독 여부
	 * @return 게시판 구독 설정 목록 (각 게시판에 대해, 유저가 구독 중인지 여부 포함)
	 */
	private List<BoardSubscribeSetting> getBoardSubscribeSettings(
		User user,
		List<Board> boards,
		boolean defaultSubscribed) {
		if (boards.isEmpty()) {
			return List.of();
		}
		Set<String> subscribedBoardIds = userBoardSubscribeReader.findSubscribedBoardIds(
			user, boards, defaultSubscribed);

		return boards.stream()
			.map(b -> new BoardSubscribeSetting(b.getId(), b.getName(), subscribedBoardIds.contains(b.getId())))
			.toList();
	}

	/**
	 * 시스템 공지 게시판(is_notice=false, is_system_notice=true)은 소통 게시판 목록에서 제외한다.
	 */
	private List<Board> excludeSystemNoticeBoards(List<Board> boards) {
		if (boards.isEmpty()) {
			return boards;
		}
		Map<String, BoardConfig> configMap = boardConfigReader.getBoardConfigMapByBoardIds(
			boards.stream().map(Board::getId).toList());
		return boards.stream()
			.filter(board -> {
				BoardConfig config = configMap.get(board.getId());
				return config != null && !config.isSystemNotice();
			})
			.toList();
	}
}
