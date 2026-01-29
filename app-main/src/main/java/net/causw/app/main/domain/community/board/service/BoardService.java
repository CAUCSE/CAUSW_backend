package net.causw.app.main.domain.community.board.service;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;
import net.causw.app.main.domain.community.board.service.dto.result.BoardListResult;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {
	private final BoardReader boardReader;
	private final BoardConfigReader boardConfigReader;

	public BoardListResult getAllBoardList(BoardQueryCondition boardQueryCondition) {
		List<Board> allBoardList = boardReader.searchBoardList(boardQueryCondition);
		List<BoardConfig> boardConfigs = boardConfigReader.getAllBoardConfigInBoardIds(
			allBoardList.stream().map(Board::getId).toList()
		);
		var boardIdBoardConfigMap = getCollectedMap(boardConfigs);

		return BoardListResult.from(
			allBoardList,
			boardIdBoardConfigMap
		);
	}

	private static @NotNull Map<String, BoardConfig> getCollectedMap(List<BoardConfig> boardConfigs) {
		return boardConfigs.stream().collect(
			java.util.stream.Collectors.toMap(BoardConfig::getBoardId, boardConfig -> boardConfig)
		);
	}
}
