package net.causw.app.main.domain.community.board.service.implementation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardGroup;
import net.causw.app.main.domain.community.common.service.CommunityPermissionPolicy;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;

import lombok.RequiredArgsConstructor;

/** 게시판 목록과 실제 액션이 동일한 권한 정책을 사용하도록 접근 가능한 게시판을 계산합니다. */
@Component
@RequiredArgsConstructor
public class BoardAccessManager {

	private final BoardReader boardReader;
	private final BoardConfigReader boardConfigReader;

	public List<Board> getReadableBoards(User viewer, BoardGroup boardGroup) {
		return getAccessibleBoards(
			viewer,
			config -> isMatchingGroup(config.isNotice(), boardGroup),
			(board, config, adminIds) -> CommunityPermissionPolicy.canReadBoard(
				viewer, board, config, adminIds));
	}

	public void validateCanRead(User user, Board board) {
		BoardConfig config = boardConfigReader.getByBoardId(board.getId());
		Set<String> adminIds = boardConfigReader.getAdminIdSetMapByBoardIds(List.of(board.getId()))
			.getOrDefault(board.getId(), Set.of());
		if (!CommunityPermissionPolicy.canReadBoard(user, board, config, adminIds)) {
			throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
		}
	}

	public List<Board> getWritableBoards(User writer, BoardGroup boardGroup) {
		return getAccessibleBoards(
			writer,
			config -> isMatchingGroup(config.isNotice(), boardGroup),
			(board, config, adminIds) -> CommunityPermissionPolicy.canWriteBoard(
				writer, board, config, adminIds));
	}

	private List<Board> getAccessibleBoards(
		User user,
		Predicate<BoardConfig> configFilter,
		BoardPermission boardPermission) {

		CommunityPermissionPolicy.validateActiveUser(user);

		List<BoardConfig> configs = boardConfigReader.getAllOrderByDisplayOrder().stream()
			.filter(configFilter)
			.toList();
		if (configs.isEmpty()) {
			return List.of();
		}

		List<String> boardIds = configs.stream().map(BoardConfig::getBoardId).toList();
		Map<String, Board> boardMap = boardReader.findAllByIdsNotDeleted(boardIds).stream()
			.collect(Collectors.toMap(Board::getId, board -> board));
		Map<String, Set<String>> adminIdMap = boardConfigReader.getAdminIdSetMapByBoardIds(boardIds);

		return configs.stream()
			.map(config -> new BoardWithConfig(boardMap.get(config.getBoardId()), config))
			.filter(item -> item.board() != null)
			.filter(item -> boardPermission.test(
				item.board(),
				item.config(),
				adminIdMap.getOrDefault(item.board().getId(), Set.of())))
			.map(BoardWithConfig::board)
			.toList();
	}

	// tb_board_config의 is_notice 값과 요청받은 boardGroup을 매핑
	private boolean isMatchingGroup(boolean isNoticeConfig, BoardGroup requestGroup) {
		if (requestGroup == null) {
			return true;
		}
		if (requestGroup == BoardGroup.NOTICE) {
			return isNoticeConfig;
		} else if (requestGroup == BoardGroup.COMMUNITY) {
			return !isNoticeConfig;
		}
		return false;
	}

	@FunctionalInterface
	private interface BoardPermission {
		boolean test(Board board, BoardConfig config, Set<String> boardAdminIds);
	}

	private record BoardWithConfig(Board board, BoardConfig config) {
	}
}
