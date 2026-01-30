package net.causw.app.main.domain.community.board.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.repository.BoardRepository;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigUpdateCommand;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardWriter {

	private final BoardRepository boardRepository;

	public Board save(Board board) {
		return boardRepository.save(board);
	}

	public void updateBoard(Board board, BoardConfigUpdateCommand command) {
		board.update(
			command.name(),
			command.description(),
			board.getCreateRoles(),
			board.getCategory());

		boardRepository.save(board);
	}
}
