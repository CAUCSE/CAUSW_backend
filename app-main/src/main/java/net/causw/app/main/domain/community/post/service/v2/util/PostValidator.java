package net.causw.app.main.domain.community.post.service.v2.util;

import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.util.UserRoleIsNoneValidator;
import net.causw.app.main.domain.user.account.util.UserStateValidator;
import net.causw.app.main.shared.ValidatorBucket;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;
import net.causw.app.main.shared.util.TargetIsDeletedValidator;
import net.causw.global.constant.StaticValue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostValidator {

	public static void validateCreate(User creator, Board board, BoardConfig boardConfig,
		List<String> boardAdminIds) {
		validateUserAndBoard(creator, board);
		validateWriteScope(creator, boardConfig, boardAdminIds);

	}

	public static void validateDelete(User deleter, Post post, List<String> adminIds) {
		Set<Role> roles = deleter.getRoles();
		if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
			// 관리자 역할이 없고, 게시글의 작성자가 아니면 오류 발생
			if (!adminIds.contains(deleter.getId())
				&& !post.getWriter().getId().equals(deleter.getId())) {
				throw PostErrorCode.POST_FORBIDDEN.toBaseException();
			}
		}
		validateUserAndBoard(deleter, post.getBoard());
	}

	public static void validateUserAndBoard(User user, Board board) {
		ValidatorBucket validatorBucket = ValidatorBucket.of();
		validatorBucket
			.consistOf(UserStateValidator.of(user.getState()))
			.consistOf(UserRoleIsNoneValidator.of(user.getRoles()))
			.consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD));
		validatorBucket.validate();
	}

	private static void validateWriteScope(User creator, BoardConfig boardConfig, List<String> boardAdminIds) {
		BoardWriteScope writeScope = boardConfig.getWriteScope();
		if (writeScope == BoardWriteScope.ALL_USER) {
			return;
		}

		if (boardAdminIds.contains(creator.getId())) {
			return;
		}

		throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
	}
}
