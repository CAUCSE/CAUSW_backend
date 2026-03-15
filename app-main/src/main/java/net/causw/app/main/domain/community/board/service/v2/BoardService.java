package net.causw.app.main.domain.community.board.service.v2;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.board.service.v2.dto.BoardReadableItemResult;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

	private final BoardReader boardReader;
	private final BoardConfigReader boardConfigReader;
	private final UserReader userReader;

	/**
	 * 특정 사용자가 읽기 가능한 게시판의 id, name 목록을 표시 순서대로 반환합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 읽기 가능한 게시판 목록 (id, name)
	 */
	public List<BoardReadableItemResult> getReadableBoards(String userId) {
		List<String> boardIds = boardConfigReader.getAccessibleBoardIdsByAcademicStatus(
			userReader.findUserById(userId).getAcademicStatus());
		if (boardIds.isEmpty()) {
			return List.of();
		}

		List<Board> boards = boardReader.findAllByIdsNotDeleted(boardIds);
		return boards.stream()
			.map(b -> new BoardReadableItemResult(b.getId(), b.getName()))
			.toList();
	}
}
