package net.causw.app.main.domain.community.post.service.v2;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.v2.util.LikePostValidator;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostWriter;
import net.causw.app.main.domain.notification.notification.event.PostLikedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikePostService {

	private final PostReader postReader;
	private final LikePostWriter likePostWriter;
	private final LikePostValidator likePostValidator;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 게시글 좋아요 메서드
	 * @param userId 좋아요 누른 유저 id
	 * @param postId 좋아요 누른 게시글 아이디
	 */
	@Transactional
	public void likePost(String userId, String postId) {
		Post post = postReader.findById(postId);

		likePostValidator.validateForLike(userId, postId);

		likePostWriter.saveLikePost(userId, post);

		// 좋아요 알림 이벤트
		eventPublisher.publishEvent(new PostLikedEvent(postId, userId));
	}

	/**
	 * 게시글 좋아요 취소 메서드
	 * @param userId 좋아요 취소 누른 유저 id
	 * @param postId 좋아요 취소 누른 게시글 아이디
	 */
	@Transactional
	public void cancelLikePost(final String userId, final String postId) {
		likePostValidator.validateForCancelLike(userId, postId);

		likePostWriter.deleteLikePost(userId, postId);
	}
}
