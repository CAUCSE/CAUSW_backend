package net.causw.app.main.domain.community.board.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.repository.BoardQueryRepository;
import net.causw.app.main.domain.community.board.repository.BoardRepository;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardReader {
	private final BoardQueryRepository boardQueryRepository;
	private final BoardRepository boardRepository;

	public List<Board> searchBoardList(BoardQueryCondition boardQueryCondition) {

		return boardQueryRepository.findWithConditionOrderByDisplayOrder(boardQueryCondition);
	}

	public Board getById(String boardId) {

		return boardQueryRepository.findById(boardId)
			.orElseThrow(() -> new BaseRunTimeV2Exception(BoardErrorCode.BOARD_NOT_FOUND));
	}

	public boolean existsByName(String name) {
		return Boolean.TRUE.equals(boardRepository.existsByName(name));
	}

	public boolean existsByNameExcludingId(String name, String excludeBoardId) {
		return Boolean.TRUE.equals(boardRepository.existsByNameAndIdNot(name, excludeBoardId));
	}
}
