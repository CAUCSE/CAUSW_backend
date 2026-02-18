package net.causw.app.main.domain.community.board.service.implementation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.repository.BoardAdminQueryRepository;
import net.causw.app.main.domain.community.board.repository.BoardConfigQueryRepository;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.BoardConfigErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardConfigReader {
	private final BoardConfigQueryRepository boardConfigQueryRepository;
	private final BoardAdminQueryRepository boardAdminQueryRepository;

	public List<BoardConfig> getAllBoardConfigInBoardIds(List<String> boardIds) {
		return boardConfigQueryRepository.findByBoardIdsIn(boardIds);
	}

	/**
	 * 게시판 ID 목록에 해당하는 설정을 boardId 기준 Map으로 반환.
	 */
	public Map<String, BoardConfig> getBoardConfigMapByBoardIds(List<String> boardIds) {

		return boardConfigQueryRepository.findByBoardIdsIn(boardIds).stream()
			.collect(Collectors.toMap(BoardConfig::getBoardId, config -> config));
	}

	/**
	 * 게시판 ID로 게시판 설정 조회
	 * @param boardId 게시판 ID
	 * @return 게시판 설정 Entity
	 */
	public BoardConfig getByBoardId(String boardId) {

		return boardConfigQueryRepository.findByBoardId(boardId).orElseThrow(
			() -> new BaseRunTimeV2Exception(BoardConfigErrorCode.BOARD_CONFIG_NOT_FOUND));
	}

	/**
	 * 게시판 ID로 해당 게시판의 관리자 유저 ID 목록 조회
	 * @param boardId 게시판 ID
	 * @return 관리자 유저 ID 목록
	 */
	public List<String> getAdminIdsByBoardId(String boardId) {

		return boardAdminQueryRepository.findAdminIdsByBoardId(boardId);
	}

	/**
	 * 다음 게시판 표시 순서 값 조회
	 * @return 다음 게시판 표시 순서 값
	 */
	public int getNextDisplayOrder() {

		return boardConfigQueryRepository.findMaxDisplayOrder() + 10;
	}

	/**
	 * 사용자 상태에 따라 접근 가능한 게시판 ID 목록을 조회합니다.
	 * VISIBLE이고 사용자의 ReadScope에 맞는 게시판만 조회합니다.
	 *
	 * @param academicStatus 사용자 상태
	 * @return 게시판 ID 목록
	 */
	public List<String> getAccessibleBoardIdsByAcademicStatus(AcademicStatus academicStatus) {
		Set<BoardReadScope> scopes = new HashSet<>();
		scopes.add(BoardReadScope.BOTH);
		switch (academicStatus) {
			case GRADUATED -> scopes.add(BoardReadScope.GRADUATED);
			case ENROLLED -> scopes.add(BoardReadScope.ENROLLED);
		}
		return boardConfigQueryRepository.findBoardsByReadScopes(scopes);
	}
}
