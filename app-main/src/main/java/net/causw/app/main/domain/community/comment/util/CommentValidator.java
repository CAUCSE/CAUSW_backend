package net.causw.app.main.domain.community.comment.util;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.implementation.LikeCommentReader;
import net.causw.app.main.domain.community.common.service.CommunityPermissionPolicy;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
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
	public void validateForCreate(User creator, Post post, BoardConfig boardConfig, List<String> boardAdminIds) {
		if (boardConfig.isSystemNotice()) {
			throw CommentErrorCode.COMMENT_NOT_ALLOWED_ON_SYSTEM_NOTICE.toBaseException();
		}
		validateForFind(creator, post, boardConfig, boardAdminIds);
		if (!CommunityPermissionPolicy.canWriteBoard(
			creator, post.getBoard(), boardConfig, boardAdminIds)) {
			throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
		}
	}

	public void validateChildCommentDepth(Comment parentComment) {
		if (parentComment != null && parentComment.isChildComment()) {
			throw CommentErrorCode.COMMENT_NOT_FOUND.toBaseException();
		}
	}

	/**
	 * 댓글 리스트 조회 시 필요한 모든 검증 로직을 수행합니다.
	 */
	public void validateForFind(User viewer, Post post, BoardConfig boardConfig, List<String> boardAdminIds) {
		CommunityPermissionPolicy.validateActiveUser(viewer);
		validatePostAlive(post);
		if (!CommunityPermissionPolicy.canReadPost(viewer, post, boardConfig, boardAdminIds)) {
			throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
		}
	}

	/**
	 * 댓글 수정 시 필요한 모든 검증 로직을 수행합니다.
	 */
	public void validateForUpdate(User updater, Comment comment, BoardConfig boardConfig,
		List<String> boardAdminIds) {
		CommunityPermissionPolicy.validateActiveUser(updater);
		validateCommentAlive(comment);
		if (!CommunityPermissionPolicy.canUpdateComment(updater, comment, boardConfig, boardAdminIds)) {
			throw AuthErrorCode.NO_PERMISSION_FOR_RESOURCE.toBaseException();
		}
	}

	/**
	 * 댓글 삭제 시 필요한 검증 로직을 수행합니다.
	 */
	public void validateForDelete(User deleter, Comment comment, BoardConfig boardConfig,
		List<String> boardAdminIds) {
		CommunityPermissionPolicy.validateActiveUser(deleter);
		validateCommentAlive(comment);
		if (!CommunityPermissionPolicy.canDeleteComment(deleter, comment, boardConfig, boardAdminIds)) {
			throw AuthErrorCode.NO_PERMISSION_FOR_RESOURCE.toBaseException();
		}
	}

	public void validateForLike(User user, Comment comment) {
		CommunityPermissionPolicy.validateActiveUser(user);
		validateCommentAlive(comment);
		if (likeCommentReader.isCommentLiked(user, comment.getId())) {
			throw CommentErrorCode.COMMENT_ALREADY_LIKED.toBaseException();
		}
	}

	public void validateForCancelLike(User user, Comment comment) {
		CommunityPermissionPolicy.validateActiveUser(user);
		validateCommentAlive(comment);
		if (!likeCommentReader.isCommentLiked(user, comment.getId())) {
			throw CommentErrorCode.COMMENT_NOT_LIKE.toBaseException();
		}
	}

	/**
	 * 작성자의 권한/상태 및 상위 게시글/게시판의 삭제 여부를 확인합니다.
	 */
	private void validatePostAlive(Post post) {
		if (!CommunityPermissionPolicy.isAlive(post.getBoard()))
			throw BoardErrorCode.BOARD_DELETED.toBaseException();
		if (!CommunityPermissionPolicy.isAlive(post))
			throw PostErrorCode.POST_NOT_FOUND.toBaseException();
	}

	private void validateCommentAlive(Comment comment) {
		if (!CommunityPermissionPolicy.isAlive(comment)) {
			throw CommentErrorCode.COMMENT_NOT_FOUND.toBaseException();
		}
	}

}
