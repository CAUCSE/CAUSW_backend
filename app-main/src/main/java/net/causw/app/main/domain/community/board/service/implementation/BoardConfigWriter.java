package net.causw.app.main.domain.community.board.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.BoardAdmin;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.repository.BoardAdminRepository;
import net.causw.app.main.domain.community.board.repository.BoardConfigRepository;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigUpdateCommand;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardConfigWriter {

	private final BoardConfigRepository boardConfigRepository;
	private final BoardAdminRepository boardAdminRepository;

	public void replaceAdmins(String boardId, List<String> adminUserIds) {
		boardAdminRepository.deleteByBoardId(boardId);
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
			command.visibility()
		);

		boardConfigRepository.save(boardConfig);
	}

	public BoardConfig save(BoardConfig boardConfig) {
		return boardConfigRepository.save(boardConfig);
	}
}
