package net.causw.app.main.facade.post.query;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.validation.TargetIsDeletedValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;
import net.causw.app.main.dto.comment.CommentResponseDto;
import net.causw.app.main.dto.post.PostResponseDto;
import net.causw.app.main.mapper.PostResponseMapper;
import net.causw.app.main.service.comment.CommentService;
import net.causw.app.main.service.post.PostFavoriteService;
import net.causw.app.main.service.post.PostLikeService;
import net.causw.app.main.service.post.PostService;
import net.causw.app.main.service.post.PostSubscribeService;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FindByPostIdFacade {

	private final PostService postService;
	private final CommentService commentService;
	private final PostResponseMapper postResponseMapper;
	private final PostLikeService postLikeService;
	private final PostFavoriteService postFavoriteService;
	private final PostSubscribeService postSubscribeService;

	public PostResponseDto execute(User user, String postId) {
		Post post = postService.getPostById(postId);

		validateUserPermission(user, post);

		Page<CommentResponseDto> commentsPage = commentService.findCommentsByPostIdByPage(user, post, 0);
		PostStats stats = collectPostStats(user, post);

		return postResponseMapper.toPostResponseDto(
			post,
			user,
			commentsPage,
			stats.numOfComments(),
			stats.numOfLikes(),
			stats.numOfFavorites(),
			stats.isLiked(),
			stats.isFavorited(),
			stats.hasComments(),
			stats.isSubscribed()
		);
	}

	private void validateUserPermission(User user, Post post) {
		ValidatorBucket validatorBucket = postService.initializeValidator(user, post.getBoard());
		validatorBucket.consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST));
		validatorBucket.validate();
	}

	// 내부 레코드로 데이터 그룹핑
	private record PostStats(
		Long numOfComments,
		Long numOfLikes,
		Long numOfFavorites,
		boolean isLiked,
		boolean isFavorited,
		boolean hasComments,
		Boolean isSubscribed
	) {}


	private PostStats collectPostStats(User user, Post post) {
		String postId = post.getId();
		return new PostStats(
			commentService.getNumOfComments(post),
			postLikeService.getNumOfPostLikes(post),
			postFavoriteService.getNumOfPostFavorites(post),
			postLikeService.getIsPostLiked(user, postId),
			postFavoriteService.getIsPostAlreadyFavorite(user, postId),
			commentService.isPostHasComment(postId),
			postSubscribeService.isPostSubscribed(user, post)
		);
	}
}
