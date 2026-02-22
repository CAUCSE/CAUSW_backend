package net.causw.app.main.domain.notification.notification.service.v2.implementation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.repository.BoardConfigRepository;
import net.causw.app.main.domain.community.board.repository.BoardRepository;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.entity.UserNotificationSetting;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeRepository;
import net.causw.app.main.domain.notification.notification.repository.UserNotificationSettingRepository;
import net.causw.app.main.domain.notification.notification.service.v2.dto.NotificationSettingResult;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.NotificationSettingErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingReader {

	private final UserNotificationSettingRepository userNotificationSettingRepository;
	private final BoardConfigRepository boardConfigRepository;
	private final BoardRepository boardRepository;
	private final UserBoardSubscribeRepository userBoardSubscribeRepository;

	/**
	 * userId 기준으로 저장된 설정을 Map으로 반환한다.
	 * DB에 row가 없으면 enum의 defaultEnabled를 사용한다.
	 */
	public Map<UserNotificationSettingKey, Boolean> findSettingMap(String userId) {
		List<UserNotificationSetting> stored = userNotificationSettingRepository.findAllByUserId(userId);
		Map<UserNotificationSettingKey, Boolean> storedMap = stored.stream()
			.collect(Collectors.toMap(
				UserNotificationSetting::getSettingKey,
				UserNotificationSetting::isEnabled));

		Map<UserNotificationSettingKey, Boolean> result = new EnumMap<>(UserNotificationSettingKey.class);
		for (UserNotificationSettingKey key : UserNotificationSettingKey.values()) {
			result.put(key, storedMap.getOrDefault(key, key.isDefaultEnabled()));
		}
		return result;
	}

	/**
	 * is_notice=true 게시판 목록과 해당 유저의 구독 여부를 함께 반환한다.
	 */
	public List<NotificationSettingResult.OfficialBoardSetting> findOfficialBoardSettings(User user) {
		List<BoardConfig> noticeConfigs = boardConfigRepository.findAllByIsNoticeTrue();
		if (noticeConfigs.isEmpty()) {
			return List.of();
		}

		List<String> boardIds = noticeConfigs.stream()
			.map(BoardConfig::getBoardId)
			.toList();

		List<Board> boards = boardRepository.findAllById(boardIds);

		List<UserBoardSubscribe> subscriptions = userBoardSubscribeRepository.findByUserAndBoardIn(user, boards);
		Set<String> subscribedBoardIds = subscriptions.stream()
			.filter(s -> Boolean.TRUE.equals(s.getIsSubscribed()))
			.map(s -> s.getBoard().getId())
			.collect(Collectors.toSet());

		return boards.stream()
			.filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()))
			.map(b -> new NotificationSettingResult.OfficialBoardSetting(
				b.getId(),
				b.getName(),
				subscribedBoardIds.contains(b.getId())))
			.toList();
	}

	/**
	 * boardId가 공식계정(is_notice=true) 게시판인지 검증하고 Board 엔티티를 반환한다.
	 */
	public Board findNoticeBoardOrThrow(String boardId) {
		if (!boardConfigRepository.existsByBoardIdAndIsNoticeTrue(boardId)) {
			throw NotificationSettingErrorCode.BOARD_NOT_NOTICE.toBaseException();
		}
		return boardRepository.findById(boardId)
			.orElseThrow(NotificationSettingErrorCode.BOARD_NOT_FOUND::toBaseException);
	}
}
