package net.causw.app.main.domain.notification.notification.service.v2.implementation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.notification.notification.service.v2.dto.OfficialBoardSetting;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.NotificationSettingErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfficialBoardSettingReader {

	private final BoardConfigReader boardConfigReader;
	private final BoardReader boardReader;
	private final UserBoardSubscribeReader userBoardSubscribeReader;

	/**
	 * 공지사항 게시판이 맞는지 검증하고, 게시판을 반환한다.
	 * @param boardId 게시판 ID
	 * @return 게시판
	 */
	public Board findNoticeBoardOrThrow(String boardId) {
		if (!boardConfigReader.existsByBoardIdAndIsNotice(boardId)) {
			throw NotificationSettingErrorCode.BOARD_NOT_NOTICE.toBaseException();
		}
		return boardReader.getById(boardId);
	}

	/**
	 * 공지사항 게시판 리스트와 유저의 구독 상태를 반환한다.
	 * @param user 조회 대상 유저
	 * @return 공지사항 게시판 리스트와 유저의 구독 상태 리스트
	 */
	public List<OfficialBoardSetting> findAll(User user) {

		List<BoardReadScope> boardReadScopes = boardReader.getReadeScopesByAcademicStatus(user.getAcademicStatus());

		List<String> boardIds = boardConfigReader.findAllNoticeConfigsByReadScope(new HashSet<>(boardReadScopes)).stream()
			.map(BoardConfig::getBoardId)
			.toList();
		if (boardIds.isEmpty()) {
			return List.of();
		}

		List<Board> boards = boardReader.findAllByIds(boardIds).stream()
			.filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()))
			.toList();
		if (boards.isEmpty()) {
			return List.of();
		}

		Set<String> subscribedBoardIds = userBoardSubscribeReader.findSubscribedBoardIds(user, boards);

		return boards.stream()
			.map(b -> new OfficialBoardSetting(b.getId(), b.getName(), subscribedBoardIds.contains(b.getId())))
			.toList();
	}
}
