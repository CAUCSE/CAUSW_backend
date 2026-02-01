package net.causw.app.main.domain.community.board.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.dto.mapper.BoardConfigDetailMapper;
import net.causw.app.main.domain.community.board.service.dto.mapper.BoardConfigPartMapper;
import net.causw.app.main.domain.community.board.service.dto.mapper.BoardConfigSummaryMapper;
import net.causw.app.main.domain.community.board.service.dto.mapper.BoardPartMapper;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigPart;
import net.causw.app.main.domain.community.board.service.dto.request.BoardOrderUpdateCommand;
import net.causw.app.main.domain.community.board.service.dto.request.BoardPart;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigDetail;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigListResult;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigSummary;
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

	private final BoardReader boardReader;
	private final BoardConfigReader boardConfigReader;
	private final BoardWriter boardWriter;
	private final BoardConfigWriter boardConfigWriter;
	private final UserReader userReader;
	private final BoardConfigDetailMapper boardConfigDetailMapper;
	private final BoardConfigSummaryMapper boardConfigSummaryMapper;
	private final BoardPartMapper boardPartMapper;
	private final BoardConfigPartMapper boardConfigPartMapper;

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

		List<BoardConfigSummary> summaries = IntStream.range(0, allBoardList.size())
			.mapToObj(i -> {
				Board board = allBoardList.get(i);
				BoardConfig config = boardIdBoardConfigMap.get(board.getId());
				return boardConfigSummaryMapper.fromEntity((long)i + 1, board, config);
			})
			.toList();

		return BoardConfigListResult.builder()
			.boards(summaries)
			.build();
	}

	/**
	 * 게시판 설정 편집 화면에 필요한 정보를 조회
	 * @param boardId 게시판 아이디
	 * @return 게시판 설정 상세 조회 결과 DTO
	 */
	public BoardConfigDetail getBoardConfigEditInfo(String boardId) {
		Board board = boardReader.getById(boardId);
		BoardConfig boardConfig = boardConfigReader.getByBoardId(boardId);
		List<String> adminIds = boardConfigReader.getAdminIdsByBoardId(boardId);
		List<User> adminUsers = userReader.getUsersByIds(adminIds);

		return boardConfigDetailMapper.fromEntity(board, boardConfig, adminUsers);
	}

	/**
	 * 게시판 생성
	 * @param board BoardPart (이름, 설명)
	 * @param config BoardConfigPart (설정 필드)
	 * @param adminUserIds 관리자 사용자 아이디 목록
	 */
	@Transactional
	public void createBoard(BoardPart board, BoardConfigPart config, List<String> adminUserIds) {
		Board boardEntity = boardPartMapper.toEntity(board);
		Board savedBoard = boardWriter.save(boardEntity);
		String boardId = savedBoard.getId();

		int displayOrder = boardConfigReader.getNextDisplayOrder();
		BoardConfig boardConfig = boardConfigPartMapper.toEntity(config, boardId, displayOrder);
		boardConfigWriter.save(boardConfig);
		boardConfigWriter.replaceAdmins(boardId, new HashSet<>(adminUserIds));
	}

	/**
	 * 게시판 설정 수정
	 * @param boardId 경로의 게시판 아이디
	 * @param board BoardPart (이름, 설명)
	 * @param config BoardConfigPart (설정 필드)
	 * @param adminUserIds 관리자 사용자 아이디 목록
	 */
	@Transactional
	public void updateBoard(String boardId, BoardPart board, BoardConfigPart config, List<String> adminUserIds) {
		Board boardEntity = boardReader.getById(boardId);
		BoardConfig boardConfig = boardConfigReader.getByBoardId(boardId);

		boardWriter.updateBoard(boardEntity, board);
		boardConfigWriter.updateBoardConfig(boardConfig, config);
		boardConfigWriter.replaceAdmins(boardId, new HashSet<>(adminUserIds));
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
		boardConfigWriter.updateDisplayOrders(command.boardIds());
	}

	private static @NotNull Map<String, BoardConfig> getCollectedMap(List<BoardConfig> boardConfigs) {
		return boardConfigs.stream().collect(
			Collectors.toMap(BoardConfig::getBoardId, boardConfig -> boardConfig));
	}
}
