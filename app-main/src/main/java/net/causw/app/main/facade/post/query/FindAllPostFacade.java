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
import net.causw.app.main.mapper.PostResponseMapper;
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
	private final PostResponseMapper postResponseMapper;
	private final UserBoardSubscribeService userBoardSubscribeService;
	private final CommentService commentService;
	private final PostLikeService postLikeService;
	private final PostFavoriteService postFavoriteService;

	public BoardPostsResponseDto execute(User user, String boardId, Integer pageNum) {
		Set<Role> roles = user.getRoles();
		Board board = boardService.getBoard(boardId);

		ValidatorBucket validatorBucket = postService.initializeValidator(user, board);
		validatorBucket.validate();

		boolean isCircleLeader = false;
		if (roles.contains(Role.LEADER_CIRCLE)) {
			isCircleLeader = postService.getCircleLeader(board.getCircle()).getId().equals(user.getId());
		}

		boolean isFavorite = favoriteBoardService.isFavorite(user.getId(), boardId);
		boolean boardSubscribed = userBoardSubscribeService.isBoardSubscribed(user, board);
		boolean deletedItemIncluded = isCircleLeader || roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT);

		Page<Post> posts;

		if (deletedItemIncluded) {
			posts = postService.getBoardPostsWithDeleted(boardId, pageNum);
		} else {
			posts = postService.getBoardPostsWithOutDeleted(boardId, pageNum);
		}
		List<String> postIds = posts.stream().map(BaseEntity::getId).toList();

		Map<String, Long> commentCounts = commentService.getCommentCountsByPostIds(postIds);
		Map<String, Long> likeCounts = postLikeService.getLikeCountsByPostIds(postIds);
		Map<String, Long> favoriteCounts = postFavoriteService.getFavoriteCountsByPostIds(postIds);

		// 게시글 조회: 리더, 관리자, 회장인 경우 삭제된 게시글도 포함하여 조회
		return postResponseMapper.toBoardPostsResponseDto(
			board,
			roles,
			isFavorite,
			boardSubscribed,
			posts,
			commentCounts,
			likeCounts,
			favoriteCounts
		);
	}
}
