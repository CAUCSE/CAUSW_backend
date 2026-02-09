package net.causw.app.main.domain.community.post.service.v2.util;

import java.util.List;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.util.UserRoleIsNoneValidator;
import net.causw.app.main.domain.user.account.util.UserStateValidator;
import net.causw.app.main.shared.ValidatorBucket;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;
import net.causw.app.main.shared.util.TargetIsDeletedValidator;
import net.causw.global.constant.StaticValue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostValidator {

	public static void validateCreate(User creator, Board board, BoardConfig boardConfig,
		List<String> boardAdminIds) {
		validateUserAndBoard(creator, board);
		validateWriteScope(creator, boardConfig, boardAdminIds);

	}

	public static void validateDelete(User deleter, Post post, List<String> adminIds) {
		if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
			// 관리자 역할이 없고, 게시글의 작성자가 아니면 오류 발생
			if (!adminIds.contains(deleter.getId())
				&& !post.getWriter().getId().equals(deleter.getId())) {
				throw PostErrorCode.POST_FORBIDDEN.toBaseException();
			}
		}
		validateUserAndBoard(deleter, post.getBoard());
	}

	public static void validateUpdate(User updater, Post post, List<String> adminIds) {
		validateUserAndBoard(updater, post.getBoard());

		// 게시글이 삭제된 경우
		if (post.getIsDeleted()) {
			throw PostErrorCode.POST_NOT_FOUND.toBaseException();
		}

		// 작성자만 수정 가능 (관리자도 타인의 게시글은 수정 불가)
		if (!post.getWriter().getId().equals(updater.getId())) {
			throw PostErrorCode.POST_FORBIDDEN.toBaseException();
		}
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

	public static void validateRead(User viewer, BoardConfig boardConfig, List<String> boardAdminIds) {
		// 게시판 관리자는 무조건 조회 가능
		if (boardAdminIds.contains(viewer.getId())) {
			return;
		}

		BoardReadScope readScope = boardConfig.getReadScope();
		AcademicStatus academicStatus = viewer.getAcademicStatus();

		// BOTH인 경우 모두 조회 가능
		if (readScope == BoardReadScope.BOTH) {
			return;
		}

		// ENROLLED인 경우 재학생만 가능
		if (readScope == BoardReadScope.ENROLLED) {
			if (academicStatus == AcademicStatus.ENROLLED
				|| academicStatus == AcademicStatus.LEAVE_OF_ABSENCE
				|| academicStatus == AcademicStatus.SUSPEND
				|| academicStatus == AcademicStatus.PROFESSOR) {
				return;
			}
			throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
		}

		// GRADUATED인 경우 졸업생만 가능
		if (readScope == BoardReadScope.GRADUATED) {
			if (academicStatus == AcademicStatus.GRADUATED) {
				return;
			}
			throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
		}

		throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
	}
}
