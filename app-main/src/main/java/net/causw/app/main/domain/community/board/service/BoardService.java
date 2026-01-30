package net.causw.app.main.domain.community.board.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigEditResult;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigListResult;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {
	private final BoardReader boardReader;
	private final BoardConfigReader boardConfigReader;
	private final UserReader userReader;

	/**
	 * 게시판 검색 조회
	 * @param boardQueryCondition 게시판 조회 조건 DTO
	 * @return 게시판 설정 목록 조회 결과 DTO
	 */
	public BoardConfigListResult getAllBoardList(BoardQueryCondition boardQueryCondition) {

		List<Board> allBoardList = boardReader.searchBoardList(boardQueryCondition);
		List<BoardConfig> boardConfigs = boardConfigReader.getAllBoardConfigInBoardIds(
			allBoardList.stream().map(Board::getId).toList());
		var boardIdBoardConfigMap = getCollectedMap(boardConfigs);

		return BoardConfigListResult.from(
			allBoardList,
			boardIdBoardConfigMap);
	}

	/**
	 * 게시판 설정 편집 화면에 필요한 정보를 조회
	 * @param boardId 게시판 아이디
	 * @return 게시판 설정 상세 조회 결과 DTO
	 */
	public BoardConfigEditResult getBoardConfigEditInfo(String boardId) {
		Board board = boardReader.getById(boardId);
		BoardConfig boardConfig = boardConfigReader.getByBoardId(boardId);
		List<String> adminIds = boardConfigReader.getAdminIdsByBoardId(boardId);
		List<User> adminUsers = userReader.getUsersByIds(adminIds);

		return BoardConfigEditResult.from(board, boardConfig, adminUsers);
	}

	private static @NotNull Map<String, BoardConfig> getCollectedMap(List<BoardConfig> boardConfigs) {
		return boardConfigs.stream().collect(
			Collectors.toMap(BoardConfig::getBoardId, boardConfig -> boardConfig));
	}
}
