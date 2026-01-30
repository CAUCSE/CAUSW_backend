package net.causw.app.main.domain.community.board.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigUpdateCommand;
import net.causw.app.main.domain.community.board.service.dto.request.BoardCreateCommand;
import net.causw.app.main.domain.community.board.service.dto.request.BoardOrderUpdateCommand;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigEditResult;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigListResult;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigWriter;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

	private static final int DISPLAY_ORDER_INTERVAL = 10;

	private final BoardReader boardReader;
	private final BoardConfigReader boardConfigReader;
	private final BoardWriter boardWriter;
	private final BoardConfigWriter boardConfigWriter;
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

	/**
	 * 게시판 생성
	 * @param command 생성할 게시판 설정
	 */
	@Transactional
	public void createBoard(BoardCreateCommand command) {
		Board board = Board.createForV2(command.name(), command.description());
		Board savedBoard = boardWriter.save(board);
		String boardId = savedBoard.getId();

		int displayOrder = boardConfigReader.getNextDisplayOrder();
		BoardConfig boardConfig = BoardConfig.of(
			boardId,
			command.isAnonymous(),
			command.readScope(),
			command.writeScope(),
			command.isNotice(),
			command.visibility(),
			displayOrder);
		boardConfigWriter.save(boardConfig);
		boardConfigWriter.replaceAdmins(boardId, new HashSet<>(command.adminUserIds()));
	}

	/**
	 * 게시판 설정 수정
	 * @param boardId 경로의 게시판 아이디
	 * @param command 수정할 게시판 설정
	 */
	@Transactional
	public void updateBoard(String boardId, BoardConfigUpdateCommand command) {
		Board board = boardReader.getById(boardId);
		BoardConfig boardConfig = boardConfigReader.getByBoardId(boardId);

		boardWriter.updateBoard(board, command);
		boardConfigWriter.updateBoardConfig(boardConfig, command);
		boardConfigWriter.replaceAdmins(boardId, new HashSet<>(command.adminUserIds()));
	}

	/**
	 * 게시판 삭제 (소프트 삭제 - isDeleted true)
	 * @param boardId 게시판 아이디
	 */
	@Transactional
	public void deleteBoard(String boardId) {
		Board board = boardReader.getById(boardId);
		board.setIsDeleted(true);
		boardWriter.save(board);
	}

	/**
	 * 게시판 정렬 순서 수정
	 * @param command 게시판 아이디 목록 (정렬 순서대로)
	 */
	@Transactional
	public void updateBoardOrder(BoardOrderUpdateCommand command) {
		List<String> boardIds = command.boardIds();
		if (boardIds == null || boardIds.isEmpty()) {
			return;
		}
		List<BoardConfig> configs = boardConfigReader.getAllBoardConfigInBoardIds(boardIds);
		Map<String, BoardConfig> boardIdToConfig = configs.stream()
			.collect(Collectors.toMap(BoardConfig::getBoardId, config -> config));

		for (int i = 0; i < boardIds.size(); i++) {
			BoardConfig config = boardIdToConfig.get(boardIds.get(i));
			if (config != null) {
				config.updateDisplayOrder((i + 1) * DISPLAY_ORDER_INTERVAL);
			}
		}
		boardConfigWriter.saveAll(new ArrayList<>(boardIdToConfig.values()));
	}

	private static @NotNull Map<String, BoardConfig> getCollectedMap(List<BoardConfig> boardConfigs) {
		return boardConfigs.stream().collect(
			Collectors.toMap(BoardConfig::getBoardId, boardConfig -> boardConfig));
	}
}
