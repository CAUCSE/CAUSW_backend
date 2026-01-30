package net.causw.app.main.domain.community.board.service.implementation;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.BoardAdmin;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.repository.BoardAdminRepository;
import net.causw.app.main.domain.community.board.repository.BoardConfigRepository;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigUpdateCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BoardConfigWriter {

	private final BoardConfigRepository boardConfigRepository;
	private final BoardAdminRepository boardAdminRepository;

	@Transactional
	public void replaceAdmins(String boardId, Set<String> adminUserIds) {
		boardAdminRepository.deleteAllByBoardId(boardId);
		boardAdminRepository.flush();
		List<BoardAdmin> newAdmins = adminUserIds.stream()
			.map(userId -> BoardAdmin.of(boardId, userId))
			.toList();
		boardAdminRepository.saveAll(newAdmins);
	}

	public void updateBoardConfig(BoardConfig boardConfig, BoardConfigUpdateCommand command) {
		boardConfig.update(
			command.isAnonymous(),
			command.readScope(),
			command.writeScope(),
			command.isNotice(),
			command.visibility());

		boardConfigRepository.save(boardConfig);
	}

	public BoardConfig save(BoardConfig boardConfig) {
		return boardConfigRepository.save(boardConfig);
	}

	public void saveAll(List<BoardConfig> boardConfigs) {
		boardConfigRepository.saveAll(boardConfigs);
	}
}
