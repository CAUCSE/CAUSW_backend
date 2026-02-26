package net.causw.app.main.domain.community.comment.util;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.implementation.LikeChildCommentReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.util.UserRoleIsNoneValidator;
import net.causw.app.main.domain.user.account.util.UserStateIsDeletedValidator;
import net.causw.app.main.domain.user.account.util.UserStateValidator;
import net.causw.app.main.shared.exception.errorcode.ChildCommentErrorCode;
import net.causw.app.main.shared.util.ConstraintValidator;
import net.causw.app.main.shared.util.ContentsAdminValidator;
import net.causw.app.main.shared.util.TargetIsDeletedValidator;
import net.causw.global.constant.StaticValue;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChildCommentValidator {

	private final Validator validator;
	private final LikeChildCommentReader likeChildCommentReader;

	/**
	 * 대댓글 생성 시 필요한 모든 검증 로직을 수행합니다.
	 */
	public void validateForCreate(User creator, Post post, Comment parentComment, ChildComment childComment) {
		this.validateCreatorAndPostStatus(creator, post);

		ConstraintValidator.of(childComment, this.validator).validate();
		UserStateIsDeletedValidator.of(parentComment.getWriter().getState());
	}

	/**
	 * 대댓글 수정 시 필요한 모든 검증 로직을 수행합니다.
	 */
	public void validateForUpdate(User updater, Post post, ChildComment childComment) {
		this.validateCreatorAndPostStatus(updater, post);

		TargetIsDeletedValidator.of(childComment.getIsDeleted(), StaticValue.DOMAIN_CHILD_COMMENT).validate();

		ConstraintValidator.of(childComment, this.validator).validate();

		ContentsAdminValidator.of(
			updater.getRoles(),
			updater.getId(),
			childComment.getWriter().getId(),
			List.of()).validate();
	}

	/**
	 * 대댓글 삭제 시 필요한 검증 로직을 수행합니다.
	 */
	public void validateForDelete(User deleter, Post post, ChildComment childComment) {
		this.validateCreatorAndPostStatus(deleter, post);

		TargetIsDeletedValidator.of(childComment.getIsDeleted(), StaticValue.DOMAIN_COMMENT).validate();
	}

	/**
	 * 작성자의 권한/상태 및 상위 게시글/게시판의 삭제 여부를 확인합니다.
	 */
	private void validateCreatorAndPostStatus(User user, Post post) {
		// 사용자 상태 및 권한 검증
		UserStateValidator.of(user.getState()).validate();
		UserRoleIsNoneValidator.of(user.getRoles()).validate();

		// 게시판 및 게시글 삭제 여부 검증
		TargetIsDeletedValidator.of(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD).validate();
		TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST).validate();
	}

	public void validateForLike(User user, ChildComment childComment) {
		UserStateIsDeletedValidator.of(childComment.getWriter().getState()).validate();
		if (likeChildCommentReader.isChildCommentLiked(user, childComment.getId())) {
			throw ChildCommentErrorCode.CHILD_COMMENT_ALREADY_LIKED.toBaseException();
		}
	}

	public void validateForCancelLike(User user, ChildComment childComment) {
		UserStateIsDeletedValidator.of(childComment.getWriter().getState()).validate();
		if (!likeChildCommentReader.isChildCommentLiked(user, childComment.getId())) {
			throw ChildCommentErrorCode.CHILD_COMMENT_NOT_LIKE.toBaseException();
		}
	}
}
