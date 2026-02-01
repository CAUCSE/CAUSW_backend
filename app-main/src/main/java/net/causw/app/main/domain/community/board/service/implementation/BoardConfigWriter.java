package net.causw.app.main.domain.community.board.service.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.BoardAdmin;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.repository.BoardAdminRepository;
import net.causw.app.main.domain.community.board.repository.BoardConfigRepository;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigPart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BoardConfigWriter {

	private static final int DISPLAY_ORDER_INTERVAL = 10;

	private final BoardConfigRepository boardConfigRepository;
	private final BoardAdminRepository boardAdminRepository;
	private final BoardConfigReader boardConfigReader;

	@Transactional
	public void replaceAdmins(String boardId, Set<String> adminUserIds) {
		boardAdminRepository.deleteAllByBoardId(boardId);
		boardAdminRepository.flush();
		List<BoardAdmin> newAdmins = adminUserIds.stream()
			.map(userId -> BoardAdmin.of(boardId, userId))
			.toList();
		boardAdminRepository.saveAll(newAdmins);
	}

	public void updateBoardConfig(BoardConfig boardConfig, BoardConfigPart config) {
		boardConfig.update(
			config.isAnonymous(),
			config.readScope(),
			config.writeScope(),
			config.isNotice(),
			config.visibility());

		boardConfigRepository.save(boardConfig);
	}

	public BoardConfig save(BoardConfig boardConfig) {
		return boardConfigRepository.save(boardConfig);
	}

	public void saveAll(List<BoardConfig> boardConfigs) {
		boardConfigRepository.saveAll(boardConfigs);
	}

	/**
	 * 게시판 정렬 순서를 주어진 boardIds 순서대로 갱신한다.
	 * display_order 간격은 {@value #DISPLAY_ORDER_INTERVAL}이다.
	 */
	@Transactional
	public void updateDisplayOrders(List<String> boardIdsInOrder) {
		if (boardIdsInOrder == null || boardIdsInOrder.isEmpty()) {
			return;
		}
		List<BoardConfig> configs = boardConfigReader.getAllBoardConfigInBoardIds(boardIdsInOrder);
		Map<String, BoardConfig> boardIdToConfig = configs.stream()
			.collect(Collectors.toMap(BoardConfig::getBoardId, config -> config));

		for (int i = 0; i < boardIdsInOrder.size(); i++) {
			BoardConfig config = boardIdToConfig.get(boardIdsInOrder.get(i));
			if (config != null) {
				config.updateDisplayOrder((i + 1) * DISPLAY_ORDER_INTERVAL);
			}
		}
		saveAll(new ArrayList<>(boardIdToConfig.values()));
	}
}
