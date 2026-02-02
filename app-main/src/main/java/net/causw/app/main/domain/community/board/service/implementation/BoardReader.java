package net.causw.app.main.domain.community.board.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.repository.BoardQueryRepository;
import net.causw.app.main.domain.community.board.repository.BoardRepository;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardReader {
	private final BoardQueryRepository boardQueryRepository;
	private final BoardRepository boardRepository;

	/**
	 * 게시판 검색 조회
	 * @param boardQueryCondition 게시판 조회 조건 DTO
	 * @return 게시판 Entity 목록
	 */
	public List<Board> searchBoardList(BoardQueryCondition boardQueryCondition) {

		return boardQueryRepository.findWithConditionOrderByDisplayOrder(boardQueryCondition);
	}

	/**
	 * 게시판 ID로 게시판 조회
	 * @param boardId 게시판 ID
	 * @return 게시판 Entity
	 */
	public Board getById(String boardId) {

		return boardQueryRepository.findById(boardId)
			.orElseThrow(BoardErrorCode.BOARD_NOT_FOUND::toBaseException);
	}

	/**
	 * 게시판 이름으로 존재 여부 조회
	 * @param name 게시판 이름
	 * @return 존재 여부
	 */
	public boolean existsByName(String name) {
		return Boolean.TRUE.equals(boardRepository.existsByName(name));
	}

	/**
	 * 게시판 이름으로, 특정 게시판 ID를 제외한 존재 여부 조회
	 * @param name 게시판 이름
	 * @param excludeBoardId 제외할 게시판 ID
	 * @return 존재 여부
	 */
	public boolean existsByNameExcludingId(String name, String excludeBoardId) {
		return Boolean.TRUE.equals(boardRepository.existsByNameAndIdNot(name, excludeBoardId));
	}
}
