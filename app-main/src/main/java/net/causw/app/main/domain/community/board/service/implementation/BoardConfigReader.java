package net.causw.app.main.domain.community.board.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.repository.BoardConfigQueryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardConfigReader {
	private final BoardConfigQueryRepository boardConfigQueryRepository;

	public List<BoardConfig> getAllBoardConfigInBoardIds(List<String> boardIds) {
		return boardConfigQueryRepository.findByBoardIdsIn(boardIds);
	}
}
