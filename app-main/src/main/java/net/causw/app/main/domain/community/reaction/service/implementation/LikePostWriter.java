package net.causw.app.main.domain.community.reaction.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.reaction.entity.LikePost;
import net.causw.app.main.domain.community.reaction.repository.LikePostRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikePostWriter {

	private final UserReader userReader;
	private final LikePostRepository likePostRepository;

	/**
	 * 사용자의 게시글에 대한 좋아요를 저장
	 */
	public void saveLikePost(String userId, Post post) {

		User user = userReader.findUserById(userId);

		LikePost likePost = LikePost.of(post, user);
		likePostRepository.save(likePost);
	}

	/**
	 * 사용자의 게시글에 대한 좋아요를 취소
	 */
	public void deleteLikePost(String userId, String postId) {
		likePostRepository.deleteLikeByPostIdAndUserId(postId, userId);
	}

}
