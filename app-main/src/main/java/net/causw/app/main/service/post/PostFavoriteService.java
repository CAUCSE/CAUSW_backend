package net.causw.app.main.service.post;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.repository.post.FavoritePostRepository;
import net.causw.app.main.repository.post.projection.PostsFavoriteCountProjection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostFavoriteService {
	private final FavoritePostRepository favoritePostRepository;

	public Map<String, Long> getFavoriteCountsByPostIds(List<String> postIds) {
		if (postIds == null || postIds.isEmpty()) {
			return new HashMap<>();
		}

		return favoritePostRepository.countByPostId(postIds)
			.stream()
			.collect(Collectors.toMap(
				PostsFavoriteCountProjection::getPostId,
				PostsFavoriteCountProjection::getFavoriteCount
			));
	}

	public Long getNumOfPostFavorites(Post post){
		return favoritePostRepository.countByPostId(post.getId());
	}

	public boolean getIsPostAlreadyFavorite(User user, String postId) {
		return favoritePostRepository.existsByPostIdAndUserId(postId, user.getId());
	}
}
