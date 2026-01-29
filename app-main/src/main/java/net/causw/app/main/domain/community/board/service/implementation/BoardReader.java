package net.causw.app.main.domain.community.board.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.repository.BoardQueryRepository;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardReader {
	private final BoardQueryRepository boardQueryRepository;

	public List<Board> searchBoardList(BoardQueryCondition boardQueryCondition) {
		return boardQueryRepository.findWithConditionOrderByDisplayOrder(boardQueryCondition);
	}
}
