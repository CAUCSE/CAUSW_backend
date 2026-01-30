package net.causw.app.main.domain.community.board.service.dto.result;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;

import lombok.Builder;

@Builder
public record BoardConfigListResult(List<BoardAdminResult> boards) {
	@Builder
	public record BoardAdminResult(
		Long no,
		String boardId,
		String name,
		String description,
		Boolean isAnonymous,
		String readScope,
		String writeScope,
		Boolean isNotice,
		String visibility,
		Integer displayOrder) {

		public static BoardAdminResult from(Long no, Board board, BoardConfig boardConfig) {
			return BoardAdminResult.builder()
				.no(no)
				.boardId(board.getId())
				.name(board.getName())
				.description(board.getDescription())
				.isAnonymous(boardConfig.isAnonymous())
				.readScope(boardConfig.getReadScope().name())
				.writeScope(boardConfig.getWriteScope().name())
				.isNotice(boardConfig.isNotice())
				.visibility(boardConfig.getVisibility().name())
				.displayOrder(boardConfig.getDisplayOrder())
				.build();
		}
	}

	public static BoardConfigListResult from(
		List<Board> boards,
		Map<String, BoardConfig> boardIdBoardConfigMap) {
		var boardAdminResults = IntStream.range(0, boards.size())
			.mapToObj(i -> BoardAdminResult.from(
				(long)i + 1,
				boards.get(i),
				boardIdBoardConfigMap.get(boards.get(i).getId())))
			.toList();

		return BoardConfigListResult.builder()
			.boards(boardAdminResults)
			.build();
	}
}
