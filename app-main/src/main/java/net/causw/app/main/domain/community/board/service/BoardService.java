package net.causw.app.main.domain.community.board.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardGroup;
import net.causw.app.main.domain.community.board.service.dto.BoardReadableItemResult;
import net.causw.app.main.domain.community.board.service.dto.BoardWritableItemResult;
import net.causw.app.main.domain.community.board.service.implementation.BoardAccessManager;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

	private final BoardAccessManager boardAccessManager;
	private final BoardConfigReader boardConfigReader;
	private final UserReader userReader;

	/**
	 * 특정 사용자가 읽기 가능한 게시판의 id, name 목록을 표시 순서대로 반환합니다.
	 *
	 * @param userId 사용자 ID
	 * @param boardGroup 게시판 그룹 Enum (NOTICE, COMMUNITY)
	 * @return 읽기 가능한 게시판 목록 (id, name)
	 */
	public List<BoardReadableItemResult> getReadableBoards(String userId, BoardGroup boardGroup) {
		User user = userReader.findUserById(userId);
		List<Board> readableBoards = boardAccessManager.getReadableBoards(user, boardGroup);
		List<String> boardIds = readableBoards.stream().map(Board::getId).toList();
		Map<String, BoardConfig> boardConfigMap = boardConfigReader.getBoardConfigMapByBoardIds(boardIds);

		return readableBoards.stream()
			.map(board -> new BoardReadableItemResult(
				board.getId(),
				board.getName(),
				boardConfigMap.get(board.getId()).isSystemNotice()))
			.toList();
	}

	/**
	 * 특정 사용자의 쓰기 권한이 있는 게시판의 id, name 목록을 표시 순서대로 반환합니다.
	 * 쓰기는 읽기 권한과 writeScope를 모두 충족해야 하며, 시스템/게시판 관리자는
	 * 공개 게시판의 scope를 우회할 수 있습니다. HIDDEN 게시판에는 누구도 작성할 수 없습니다.
	 *
	 * @param userId 사용자 ID
	 * @param boardGroup 게시판 그룹 Enum (NOTICE, COMMUNITY)
	 * @return 쓰기 가능한 게시판 목록 (id, name)
	 */
	public List<BoardWritableItemResult> getWritableBoards(String userId, BoardGroup boardGroup) {
		User user = userReader.findUserById(userId);
		return boardAccessManager.getWritableBoards(user, boardGroup).stream()
			.map(b -> new BoardWritableItemResult(b.getId(), b.getName()))
			.toList();
	}
}
