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
	private final UserBoardSubscribeService userBoardSubscribeService;
	private final CommentService commentService;
	private final PostLikeService postLikeService;
	private final PostFavoriteService postFavoriteService;

	private final BoardPostsResponseMapper boardPostsResponseMapper;

	public BoardPostsResponseDto execute(User user, String boardId, Integer pageNum) {
		Board board = boardService.getBoard(boardId);

		validateUserPermission(user, board);

		UserBoardContext context = createUserBoardContext(user, board);
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

	/**
	 * 게시판에 대한 유저의 접근권한 검증
	 * @param user 게시판 접근 유저
	 * @param board 게시판
	 */
	private void validateUserPermission(User user, Board board) {
		ValidatorBucket validatorBucket = postService.initializeValidator(user, board);
		validatorBucket.validate();
	}

	/**
	 *
	 * @param user 게시판 접근 유저
	 * @param board 게시판
	 * @return 게시판에 대한 유저의 컨텍스트 (동아리 회장 여부, 지워진 아이템 접근 가능 여부 등)
	 */
	private UserBoardContext createUserBoardContext(User user, Board board) {
		String boardId = board.getId();
		Set<Role> roles = user.getRoles();

		boolean isCircleLeader = hasCircleLeaderAccess(user, board);
		boolean hasDeletedItemAccess = isCircleLeader || hasAdminAccess(user);
		boolean isFavorite = favoriteBoardService.isFavorite(user.getId(), boardId);
		boolean isBoardSubscribed = userBoardSubscribeService.isBoardSubscribed(user, board);

		return new UserBoardContext(hasDeletedItemAccess, isFavorite, isBoardSubscribed);
	}

	/**
	 * @deprecated 현재는 동아리 관련 기능 사용하지 않고 있음
	 * @param user 게시판 접근 유저
	 * @param board 게시판
	 * @return 동아리 리더 권한 있는지 여부
	 */
	private boolean hasCircleLeaderAccess(User user, Board board) {
		Set<Role> roles = user.getRoles();

		return roles.contains(Role.LEADER_CIRCLE) &&
			postService.getCircleLeader(board.getCircle()).getId().equals(user.getId());
	}

	/**
	 * 관리자 여부 판별 메서드
	 * @param user 유저
	 * @return 관리자 권한 여부
	 */
	private boolean hasAdminAccess(User user) {
		Set<Role> roles = user.getRoles();

		return roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT);
	}

	/**
	 * 게시물 가져오는 메서드
	 * @param boardId 게시판 아이디
	 * @param pageNum 페이지 번호
	 * @param includeDeleted 삭제 게시물 포함 여부
	 * @return 게시물 페이지
	 */
	private Page<Post> retrievePosts(String boardId, Integer pageNum, boolean includeDeleted) {
		return includeDeleted
			? postService.getBoardPostsWithDeleted(boardId, pageNum)
			: postService.getBoardPostsWithOutDeleted(boardId, pageNum);
	}

	/**
	 * 게시물들에 대한 통계 (댓글수, 게시물 수, 좋아요 수 등) 확인 메서드
	 * @param posts 게시물
	 * @return 게시물 관련 통계 (댓글수, 게시물 수, 좋아요 수 등)
	 */
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
	) {
	}

	private record PostMetrics(
		Map<String, Long> commentCounts,
		Map<String, Long> likeCounts,
		Map<String, Long> favoriteCounts
	) {
	}
}