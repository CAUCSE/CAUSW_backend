package net.causw.app.main.domain.community.board.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.repository.BoardAdminQueryRepository;
import net.causw.app.main.domain.community.board.repository.BoardConfigQueryRepository;
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

	public BoardConfig getByBoardId(String boardId) {
		return boardConfigQueryRepository.findByBoardId(boardId).orElseThrow(
			() -> new BaseRunTimeV2Exception(BoardConfigErrorCode.BOARD_CONFIG_NOT_FOUND));
	}

	public List<String> getAdminIdsByBoardId(String boardId) {
		return boardAdminQueryRepository.findAdminIdsByBoardId(boardId);
	}

	public int getNextDisplayOrder() {
		return boardConfigQueryRepository.findMaxDisplayOrder() + 10;
	}
}
