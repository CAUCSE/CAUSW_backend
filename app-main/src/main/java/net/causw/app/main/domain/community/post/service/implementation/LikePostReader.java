package net.causw.app.main.domain.community.post.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.reaction.repository.LikePostRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikePostReader {

	private final LikePostRepository likePostRepository;

	/**
	 * 사용자가 게시글에 좋아요를 눌렀는지 확인
	 */
	public Boolean isPostLiked(String userId, String postId) {
		return likePostRepository.existsByPostIdAndUserId(postId, userId);
	}

}
