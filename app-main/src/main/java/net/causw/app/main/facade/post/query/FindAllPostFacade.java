package net.causw.app.main.facade.post.query;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.validation.ValidatorBucket;
import net.causw.app.main.dto.post.BoardPostsResponseDto;
import net.causw.app.main.mapper.BoardPostsResponseMapper;
import net.causw.app.main.service.board.BoardService;
import net.causw.app.main.service.board.FavoriteBoardService;
import net.causw.app.main.service.board.UserBoardSubscribeService;
import net.causw.app.main.service.comment.CommentService;
import net.causw.app.main.service.post.PostFavoriteService;
import net.causw.app.main.service.post.PostLikeService;
import net.causw.app.main.service.post.PostService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindAllPostFacade {

	private final PostService postService;
	private final BoardService boardService;
	private final FavoriteBoardService favoriteBoardService;
	private final BoardPostsResponseMapper boardPostsResponseMapper;
	private final UserBoardSubscribeService userBoardSubscribeService;
	private final CommentService commentService;
	private final PostLikeService postLikeService;
	private final PostFavoriteService postFavoriteService;

	public BoardPostsResponseDto execute(User user, String boardId, Integer pageNum) {
		Board board = boardService.getBoard(boardId);

		validateUserPermission(user, board);

		UserBoardContext context = createUserBoardContext(user, board, boardId);
		Page<Post> posts = retrievePosts(boardId, pageNum, context.hasDeletedItemAccess());
		PostMetrics metrics = collectPostMetrics(posts);

		return boardPostsResponseMapper.toBoardPostsResponseDto(
			board,
			user.getRoles(),
			context.isFavorite(),
			context.isBoardSubscribed(),
			posts,
			metrics.commentCounts(),
			metrics.likeCounts(),
			metrics.favoriteCounts()
		);
	}

	private void validateUserPermission(User user, Board board) {
		ValidatorBucket validatorBucket = postService.initializeValidator(user, board);
		validatorBucket.validate();
	}

	private UserBoardContext createUserBoardContext(User user, Board board, String boardId) {
		Set<Role> roles = user.getRoles();

		boolean isCircleLeader = hasCircleLeaderAccess(user, board, roles);
		boolean hasDeletedItemAccess = isCircleLeader || hasAdminAccess(roles);
		boolean isFavorite = favoriteBoardService.isFavorite(user.getId(), boardId);
		boolean isBoardSubscribed = userBoardSubscribeService.isBoardSubscribed(user, board);

		return new UserBoardContext(hasDeletedItemAccess, isFavorite, isBoardSubscribed);
	}

	private boolean hasCircleLeaderAccess(User user, Board board, Set<Role> roles) {
		return roles.contains(Role.LEADER_CIRCLE) &&
			postService.getCircleLeader(board.getCircle()).getId().equals(user.getId());
	}

	private boolean hasAdminAccess(Set<Role> roles) {
		return roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT);
	}

	private Page<Post> retrievePosts(String boardId, Integer pageNum, boolean includeDeleted) {
		return includeDeleted
			? postService.getBoardPostsWithDeleted(boardId, pageNum)
			: postService.getBoardPostsWithOutDeleted(boardId, pageNum);
	}

	private PostMetrics collectPostMetrics(Page<Post> posts) {
		List<String> postIds = posts.stream()
			.map(BaseEntity::getId)
			.toList();

		Map<String, Long> commentCounts = commentService.getCommentCountsByPostIds(postIds);
		Map<String, Long> likeCounts = postLikeService.getLikeCountsByPostIds(postIds);
		Map<String, Long> favoriteCounts = postFavoriteService.getFavoriteCountsByPostIds(postIds);

		return new PostMetrics(commentCounts, likeCounts, favoriteCounts);
	}

	// 내부 레코드들
	private record UserBoardContext(
		boolean hasDeletedItemAccess,
		boolean isFavorite,
		boolean isBoardSubscribed
	) {}

	private record PostMetrics(
		Map<String, Long> commentCounts,
		Map<String, Long> likeCounts,
		Map<String, Long> favoriteCounts
	) {}
}