package net.causw.app.main.domain.community.post.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.common.service.CommunityPermissionPolicy;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.util.LikePostValidator;
import net.causw.app.main.domain.community.post.service.util.PostValidator;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostReader;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostWriter;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneReachedEvent;
import net.causw.app.main.domain.notification.notification.service.policy.LikePostNotificationPolicy;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikePostService {

	private final PostReader postReader;
	private final LikePostReader likePostReader;
	private final LikePostWriter likePostWriter;
	private final LikePostValidator likePostValidator;
	private final ApplicationEventPublisher eventPublisher;
	private final UserReader userReader;
	private final BoardConfigReader boardConfigReader;
	private final BlockReader blockReader;

	/**
	 * 게시글 좋아요 메서드
	 * @param userId 좋아요 누른 유저 id
	 * @param postId 좋아요 누른 게시글 아이디
	 */
	@Transactional
	public void likePost(String userId, String postId) {
		User user = userReader.findUserByIdNotDeleted(userId);
		Post post = postReader.findByIdAndNotDeleted(postId);
		String boardId = post.getBoard().getId();
		BoardConfig boardConfig = boardConfigReader.getByBoardId(boardId);
		var boardAdminIds = boardConfigReader.getAdminIdsByBoardId(boardId);
		PostValidator.validateRead(user, post, boardConfig, boardAdminIds);
		validateBlockedWriterAccess(user, post, boardAdminIds);

		likePostValidator.validateForLike(userId, postId);

		likePostWriter.saveLikePost(userId, post);

		long likeCount = likePostReader.countByPostId(postId);
		if (LikePostNotificationPolicy.isMilestone(likeCount)) {
			eventPublisher.publishEvent(new PostLikeMilestoneReachedEvent(postId, userId, likeCount));
		}
	}

	/**
	 * 게시글 좋아요 취소 메서드
	 * @param userId 좋아요 취소 누른 유저 id
	 * @param postId 좋아요 취소 누른 게시글 아이디
	 */
	@Transactional
	public void cancelLikePost(final String userId, final String postId) {
		User user = userReader.findUserByIdNotDeleted(userId);
		CommunityPermissionPolicy.validateActiveUser(user);
		Post post = postReader.findByIdAndNotDeleted(postId);
		if (!CommunityPermissionPolicy.isAlive(post)) {
			throw BoardErrorCode.BOARD_DELETED.toBaseException();
		}
		likePostValidator.validateForCancelLike(userId, postId);

		likePostWriter.deleteLikePost(userId, postId);
	}

	private void validateBlockedWriterAccess(User viewer, Post post, java.util.List<String> boardAdminIds) {
		User writer = post.getWriter();
		if (writer == null
			|| CommunityPermissionPolicy.isModerator(viewer, boardAdminIds)) {
			return;
		}
		if (blockReader.existsByBlockerAndBlocked(viewer, writer)) {
			throw PostErrorCode.BLOCKED_USER_CONTENT.toBaseException();
		}
	}
}
