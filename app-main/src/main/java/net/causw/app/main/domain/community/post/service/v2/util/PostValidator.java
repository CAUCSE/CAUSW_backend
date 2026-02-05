package net.causw.app.main.domain.community.post.service.v2.util;

import java.util.List;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.util.UserRoleIsNoneValidator;
import net.causw.app.main.domain.user.account.util.UserStateValidator;
import net.causw.app.main.shared.ValidatorBucket;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;
import net.causw.app.main.shared.util.TargetIsDeletedValidator;
import net.causw.global.constant.StaticValue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostValidator {

	public static void validateCreate(User creator, Board board, BoardConfig boardConfig,
		List<String> boardAdminIds) {

		ValidatorBucket validatorBucket = ValidatorBucket.of();
		validatorBucket
			.consistOf(UserStateValidator.of(creator.getState()))
			.consistOf(UserRoleIsNoneValidator.of(creator.getRoles()))
			.consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD));

		validateWriteScope(creator, boardConfig, boardAdminIds);

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
