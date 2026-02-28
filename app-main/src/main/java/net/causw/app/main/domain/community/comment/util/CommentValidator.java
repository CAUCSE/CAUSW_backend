package net.causw.app.main.domain.community.comment.util;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.implementation.LikeCommentReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;
import net.causw.app.main.shared.exception.errorcode.CommentErrorCode;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentValidator {

	private final LikeCommentReader likeCommentReader;

	/**
	 * 댓글 생성 시 필요한 모든 검증 로직을 수행합니다.
	 */
	public void validateForCreate(User creator, Post post) {
		this.validateCreatorAndPostStatus(creator, post);
	}

	/**
	 * 댓글 리스트 조회 시 필요한 모든 검증 로직을 수행합니다.
	 */
	public void validateForFind(User creator, Post post) {
		this.validateCreatorAndPostStatus(creator, post);
	}

	/**
	 * 댓글 수정 시 필요한 모든 검증 로직을 수행합니다.
	 */
	public void validateForUpdate(User updater, Post post, Comment comment) {
		this.validateCreatorAndPostStatus(updater, post);

		if (comment.getIsDeleted()) {
			throw CommentErrorCode.COMMENT_NOT_FOUND.toBaseException();
		}

		if (!updater.getId().equals(comment.getWriter().getId())
			&& updater.getRoles().stream()
			.noneMatch(role -> EnumSet.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT).contains(role))) {
			throw AuthErrorCode.NO_PERMISSION_FOR_RESOURCE.toBaseException();
		}
	}

	/**
	 * 댓글 삭제 시 필요한 검증 로직을 수행합니다.
	 */
	public void validateForDelete(User deleter, Post post, Comment comment) {
		this.validateCreatorAndPostStatus(deleter, post);

		if (comment.getIsDeleted()) {
			throw CommentErrorCode.COMMENT_NOT_FOUND.toBaseException();
		}
	}

	public void validateForLike(User user, Comment comment) {
		if (comment.getWriter().getState() == UserState.DELETED) {
			throw AuthErrorCode.DELETED_USER.toBaseException();
		}
		if (likeCommentReader.isCommentLiked(user, comment.getId())) {
			throw CommentErrorCode.COMMENT_ALREADY_LIKED.toBaseException();
		}
	}

	public void validateForCancelLike(User user, Comment comment) {
		if (comment.getWriter().getState() == UserState.DELETED) {
			throw AuthErrorCode.DELETED_USER.toBaseException();
		}
		if (!likeCommentReader.isCommentLiked(user, comment.getId())) {
			throw CommentErrorCode.COMMENT_NOT_LIKE.toBaseException();
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

}
