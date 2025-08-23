package net.causw.app.main.service.post;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.repository.post.LikePostRepository;
import net.causw.app.main.repository.post.projection.PostLikeCountProjection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostLikeService {
	private final LikePostRepository likePostRepository;

	public Map<String, Long> getLikeCountsByPostIds(List<String> postIds) {
		if (postIds == null || postIds.isEmpty()) {
			return new HashMap<>();
		}

		return likePostRepository.countByPostId(postIds)
			.stream()
			.collect(Collectors.toMap(
				PostLikeCountProjection::getPostId,
				PostLikeCountProjection::getLikeCount
			));
	}

	public Long getNumOfPostLikes(Post post){
		return likePostRepository.countByPostId(post.getId());
	}

	public boolean getIsPostLiked(User user, String postId) {
		return likePostRepository.existsByPostIdAndUserId(postId, user.getId());
	}
}
