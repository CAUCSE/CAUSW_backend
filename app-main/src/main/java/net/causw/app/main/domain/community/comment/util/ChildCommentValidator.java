package net.causw.app.main.domain.community.comment.util;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.implementation.LikeChildCommentReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;
import net.causw.app.main.shared.exception.errorcode.ChildCommentErrorCode;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChildCommentValidator {

	private final LikeChildCommentReader likeChildCommentReader;

	/**
	 * 대댓글 생성 시 필요한 모든 검증 로직을 수행합니다.
	 */
	public void validateForCreate(User creator, Post post, Comment parentComment) {
		this.validateCreatorAndPostStatus(creator, post);

		if (parentComment.getWriter().getState() == UserState.DELETED) {
			throw AuthErrorCode.DELETED_USER.toBaseException();
		}
	}

	/**
	 * 대댓글 수정 시 필요한 모든 검증 로직을 수행합니다.
	 */
	public void validateForUpdate(User updater, Post post, ChildComment childComment) {
		this.validateCreatorAndPostStatus(updater, post);

		if (childComment.getIsDeleted()) {
			throw ChildCommentErrorCode.CHILD_COMMENT_NOT_FOUND.toBaseException();
		}

		if (!updater.getId().equals(childComment.getWriter().getId())
			&& updater.getRoles().stream()
			.noneMatch(role -> EnumSet.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT).contains(role))) {
			throw AuthErrorCode.NO_PERMISSION_FOR_RESOURCE.toBaseException();
		}
	}

	/**
	 * 대댓글 삭제 시 필요한 검증 로직을 수행합니다.
	 */
	public void validateForDelete(User deleter, Post post, ChildComment childComment) {
		this.validateCreatorAndPostStatus(deleter, post);

		if (childComment.getIsDeleted()) {
			throw ChildCommentErrorCode.CHILD_COMMENT_NOT_FOUND.toBaseException();
		}
	}

	/**
	 * 작성자의 권한/상태 및 상위 게시글/게시판의 삭제 여부를 확인합니다.
	 */
	private void validateCreatorAndPostStatus(User user, Post post) {
		UserState userState = user.getState();
		if (userState == UserState.DROP) throw AuthErrorCode.DROPPED_USER.toBaseException();
		if (userState == UserState.INACTIVE) throw AuthErrorCode.INACTIVE_USER.toBaseException();
		if (userState == UserState.DELETED) throw AuthErrorCode.DELETED_USER.toBaseException();

		if (user.getRoles().contains(Role.NONE)) throw AuthErrorCode.USER_ROLE_NONE.toBaseException();

		if (post.getBoard().getIsDeleted()) throw BoardErrorCode.BOARD_DELETED.toBaseException();
		if (post.getIsDeleted()) throw PostErrorCode.POST_NOT_FOUND.toBaseException();
	}

	/**
	 * 대댓글 좋아요 시 필요한 검증 로직을 수행합니다.
	 */
	public void validateForLike(User user, ChildComment childComment) {
		if (childComment.getWriter().getState() == UserState.DELETED) {
			throw AuthErrorCode.DELETED_USER.toBaseException();
		}
		if (likeChildCommentReader.isChildCommentLiked(user, childComment.getId())) {
			throw ChildCommentErrorCode.CHILD_COMMENT_ALREADY_LIKED.toBaseException();
		}
	}

	/**
	 * 대댓글 좋아요 취소 시 필요한 검증 로직을 수행합니다.
	 */
	public void validateForCancelLike(User user, ChildComment childComment) {
		if (childComment.getWriter().getState() == UserState.DELETED) {
			throw AuthErrorCode.DELETED_USER.toBaseException();
		}
		if (!likeChildCommentReader.isChildCommentLiked(user, childComment.getId())) {
			throw ChildCommentErrorCode.CHILD_COMMENT_NOT_LIKE.toBaseException();
		}
	}
}
