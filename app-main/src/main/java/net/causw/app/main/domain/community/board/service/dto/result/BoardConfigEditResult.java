package net.causw.app.main.domain.community.board.service.dto.result;

import java.util.List;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.Builder;

@Builder
public record BoardConfigEditResult(
	String boardId,
	String name,
	String description,
	Boolean isAnonymous,
	BoardReadScope readScope,
	BoardWriteScope writeScope,
	Boolean isNotice,
	String visibility,
	Integer displayOrder,
	List<AdminResponse> admins) {
	public record AdminResponse(
		String id,
		String adminEmail,
		String adminName) {
		public static AdminResponse from(User user) {
			return new AdminResponse(user.getId(), user.getEmail(), user.getName());
		}
	}

	public static BoardConfigEditResult from(Board board, BoardConfig boardConfig, List<User> admins) {

		List<AdminResponse> adminResponses = admins.stream()
			.map(AdminResponse::from)
			.toList();

		return BoardConfigEditResult.builder()
			.boardId(board.getId())
			.name(board.getName())
			.description(board.getDescription())
			.isAnonymous(boardConfig.isAnonymous())
			.readScope(boardConfig.getReadScope())
			.writeScope(boardConfig.getWriteScope())
			.isNotice(boardConfig.isNotice())
			.visibility(boardConfig.getVisibility().name())
			.displayOrder(boardConfig.getDisplayOrder())
			.admins(adminResponses)
			.build();
	}
}
