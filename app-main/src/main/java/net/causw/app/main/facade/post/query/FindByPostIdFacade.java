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
		Long numOfComments = commentService.getNumOfComments(post);
		Long numOfPostLikes = postLikeService.getNumOfPostLikes(post);
		Long numOfPostFavorites = postFavoriteService.getNumOfPostFavorites(post);
		boolean postLiked = postLikeService.getIsPostLiked(user, postId);
		boolean postAlreadyFavorite = postFavoriteService.getIsPostAlreadyFavorite(user, postId);
		boolean isPostHasComment = commentService.isPostHasComment(postId);
		Boolean postSubscribed = postSubscribeService.isPostSubscribed(user, post);

		return postResponseMapper.toPostResponseDto(
			post,
			user,
			commentsPage,
			numOfComments,
			numOfPostLikes,
			numOfPostFavorites,
			postLiked,
			postAlreadyFavorite,
			isPostHasComment,
			postSubscribed
		);
	}

	private void validateUserPermission(User user, Post post) {
		ValidatorBucket validatorBucket = postService.initializeValidator(user, post.getBoard());
		validatorBucket.consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST));
		validatorBucket.validate();
	}
}
