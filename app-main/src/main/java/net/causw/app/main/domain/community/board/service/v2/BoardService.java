package net.causw.app.main.domain.community.board.service.v2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.board.service.v2.dto.BoardReadableItemResult;
import net.causw.app.main.domain.community.board.service.v2.dto.BoardWritableItemResult;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
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
	 * @param isTab 탭 노출용 필터링 여부
	 * @return 읽기 가능한 게시판 목록 (id, name)
	 */
	public List<BoardReadableItemResult> getReadableBoards(String userId, boolean isTab) {
		List<String> boardIds = boardConfigReader.getAccessibleBoardIdsByAcademicStatus(
			userReader.findUserById(userId).getAcademicStatus(), isTab);
		if (boardIds.isEmpty()) {
			return List.of();
		}

		List<Board> boards = boardReader.findAllByIdsNotDeleted(boardIds);

		Map<String, Board> boardMap = boards.stream().collect(Collectors.toMap(Board::getId, b -> b));

		return boardIds.stream()
			.filter(boardMap::containsKey)
			.map(id -> new BoardReadableItemResult(boardMap.get(id).getId(), boardMap.get(id).getName()))
			.toList();
	}

	/**
	 * 특정 사용자의 쓰기 권한이 있는 게시판의 id, name 목록을 표시 순서대로 반환합니다.
	 * 관리자는 모든 게시판에 쓰기 권한이 있습니다.
	 * 사용자의 학적 상태가 졸업 또는 재학일 경우만 해당됩니다.
	 *
	 * @param userId 사용자 ID
	 * @return 쓰기 가능한 게시판 목록 (id, name)
	 */
	public List<BoardWritableItemResult> getWritableBoards(String userId) {

		User user = userReader.findUserById(userId);

		// ADMIN은 항상 권한 보유
		boolean isAdmin = user.getRoles().contains(Role.ADMIN);
		if (!isAdmin) {
			AcademicStatus academicStatus = user.getAcademicStatus();
			//졸업, 재학 academicStatus만 허용
			if (academicStatus != AcademicStatus.ENROLLED && academicStatus != AcademicStatus.GRADUATED) {
				return List.of();
			}
		}

		List<Board> boards = boardConfigReader.getWritableBoardIdsByUserId(userId, isAdmin);

		return boards.stream()
			.map(b -> new BoardWritableItemResult(b.getId(), b.getName()))
			.toList();
	}
}
