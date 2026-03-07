package net.causw.app.main.domain.community.board.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import net.causw.app.main.domain.community.board.util.BoardValidator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardAdminService {

	// TODO(v1 미적용): BoardV1Service 대비 미적용 비즈니스 로직
	// - findAllBoard: 사용자 역할/학적/동아리 기반 게시판 목록 필터링
	// - mainBoard: 메인 게시판 + 최근 게시글 조회
	// - checkBoardName: 이름 중복 체크 전용 API (v2는 create/update 시 validator로만 검사)
	// - applyBoard, findAllBoardApply, findBoardApplyByApplyId, accept, reject: 게시판 신청/승인/거부
	// - createNoticeBoard: 공지 전용 생성 (v2는 createBoard로 통합)
	// - updateBoard: ValidatorBucket 기반 권한 검증(동아리/공지 등)
	// - deleteBoard: 공지 게시판 ADMIN만 삭제 가능, 해당 게시판 게시글 일괄 삭제 처리
	// - restoreBoard: 게시판 복원(삭제 취소)
	// - createBoardSubscribe, setBoardSubscribe: 구독 생성/설정
	// - createNoticeBoardsSubscribe, updateBoardsSubscribe: 학적 인증 이벤트 기반 구독 생성/갱신

	private final BoardReader boardReader;
	private final BoardConfigReader boardConfigReader;
	private final BoardWriter boardWriter;
	private final BoardConfigWriter boardConfigWriter;
	private final UserReader userReader;
	private final BoardConfigDetailMapper boardConfigDetailMapper;
	private final BoardConfigSummaryMapper boardConfigSummaryMapper;
	private final BoardPartMapper boardPartMapper;
	private final BoardConfigPartMapper boardConfigPartMapper;
	private final BoardValidator boardValidator;

	/**
	 * 게시판 검색 조회
	 * @param boardQueryCondition 게시판 조회 조건 DTO
	 * @return 게시판 설정 목록 조회 결과 DTO
	 */
	public BoardConfigListResult getAllBoardList(BoardQueryCondition boardQueryCondition) {
		List<Board> boards = boardReader.searchBoardList(boardQueryCondition);
		Map<String, BoardConfig> configMap = boardConfigReader.getBoardConfigMapByBoardIds(
			boards.stream().map(Board::getId).toList());
		List<BoardConfigSummary> summaries = boardConfigSummaryMapper.toSummaries(boards, configMap);

		return new BoardConfigListResult(summaries);
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
		List<User> adminUsers = userReader.findUsersByIds(adminIds);

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
		// board 관련 검증
		boardValidator.validateForCreate(board.name());

		// board 생성
		Board boardEntity = boardPartMapper.toEntity(board);
		Board savedBoard = boardWriter.save(boardEntity);

		// boardConfig 생성
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
		// board 관련 검증
		boardValidator.validateForUpdate(board.name(), boardId);

		// board, boardConfig 조회
		Board boardEntity = boardReader.getById(boardId);
		BoardConfig boardConfig = boardConfigReader.getByBoardId(boardId);

		// 수정 수행
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

}
