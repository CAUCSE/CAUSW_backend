package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentResponseDto;
import net.causw.app.main.domain.community.comment.api.v2.mapper.CommentResponseDtoMapper;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.repository.CommentRepository;
import net.causw.app.main.domain.notification.notification.service.implementation.UserCommentSubscribeReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentReader {

	private final BoardConfigReader boardConfigReader;
	private final LikeCommentReader likeCommentReader;
	private final ChildCommentReader childCommentReader;
	private final LikeChildCommentReader likeChildCommentReader;
	private final UserCommentSubscribeReader userCommentSubscribeReader;
	private final CommentResponseDtoMapper commentResponseDtoMapper;
	private final CommentRepository commentRepository;

	/**
	 * @param comment 상세 조회를 수행할 대상 부모 댓글 엔티티
	 * @param user    현재 조회를 요청한 사용자
	 * @param board   댓글이 속한 게시판
	 * @return CommentResponseDto
	 */
	public CommentResponseDto getCommentDetail(Comment comment, User user, Board board) {

		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(board.getId());

		List<String> childCommentIds = comment.getChildCommentList().stream()
			.map(ChildComment::getId)
			.toList();

		Map<String, Long> childCommentLikeCounts = childCommentIds.isEmpty()
			? Collections.emptyMap()
			: likeChildCommentReader.getChildCommentLikeCounts(childCommentIds);

		Set<String> likedChildCommentIds = childCommentIds.isEmpty()
			? Collections.emptySet()
			: likeChildCommentReader.getLikedChildCommentIds(user.getId(), childCommentIds);

		List<ChildCommentResponseDto> childCommentList = comment.getChildCommentList().stream()
			.map(childComment -> childCommentReader.getChildCommentListDetails(
				childComment, user, Collections.emptySet(), boardAdminIds,
				childCommentLikeCounts.getOrDefault(childComment.getId(), 0L),
				likedChildCommentIds.contains(childComment.getId())))
			.toList();

		long numLike = likeCommentReader.getNumOfCommentLikes(comment.getId());
		boolean isLiked = likeCommentReader.isCommentLiked(user, comment.getId());
		boolean isSubscribed = userCommentSubscribeReader.isCommentSubscribed(user, comment);

		boolean isOwner = comment.getWriter().getId().equals(user.getId());
		boolean updatable = isOwner || boardAdminIds.contains(user.getId());
		boolean deletable = isOwner || boardAdminIds.contains(user.getId());

		return commentResponseDtoMapper.toCommentResponseDto(
			comment,
			(long)comment.getChildCommentList().size(),
			numLike,
			isLiked,
			isOwner,
			childCommentList,
			updatable,
			deletable,
			isSubscribed,
			false);
	}

	/**
	 * @param comment 댓글
	 * @param user 조회자
	 * @param blockedUserIds 차단된 유저 ID 목록
	 * @param boardAdminIds 게시판 관리자 목록
	 * @param commentLikeCounts 미리 조회한 부모 댓글들의 좋아요 수 Map
	 * @param likedCommentIds 현재 유저가 좋아요를 누른 부모 댓글 ID Set
	 * @param subscribedCommentIds 현재 유저가 구독한 부모 댓글 ID Set
	 * @param childCommentLikeCounts 미리 조회한 자식 댓글들의 좋아요 수 Map
	 * @param likedChildCommentIds 현재 유저가 좋아요를 누른 자식 댓글 ID Set
	 * @return CommentResponseDto
	 */
	public CommentResponseDto getCommentListDetails(
		Comment comment, User user, Set<String> blockedUserIds, List<String> boardAdminIds,
		Map<String, Long> commentLikeCounts,
		Set<String> likedCommentIds,
		Set<String> subscribedCommentIds,
		Map<String, Long> childCommentLikeCounts,
		Set<String> likedChildCommentIds) {

		boolean isBlockedContent = blockedUserIds.contains(comment.getWriter().getId());

		List<ChildCommentResponseDto> childCommentList = comment.getChildCommentList().stream()
			.map(childComment -> childCommentReader.getChildCommentListDetails(
				childComment, user, blockedUserIds, boardAdminIds,
				childCommentLikeCounts.getOrDefault(childComment.getId(), 0L),
				likedChildCommentIds.contains(childComment.getId())))
			.toList();

		long numLike = commentLikeCounts.getOrDefault(comment.getId(), 0L);
		boolean isLiked = likedCommentIds.contains(comment.getId());
		boolean isSubscribed = subscribedCommentIds.contains(comment.getId());

		boolean isOwner = comment.getWriter().getId().equals(user.getId());
		boolean updatable = isOwner || boardAdminIds.contains(user.getId());
		boolean deletable = isOwner || boardAdminIds.contains(user.getId());

		return commentResponseDtoMapper.toCommentResponseDto(
			comment,
			(long)comment.getChildCommentList().size(),
			numLike,
			isLiked,
			isOwner,
			childCommentList,
			updatable,
			deletable,
			isSubscribed,
			isBlockedContent);
	}

	/**
	 * @param commentId 댓글 id
	 * @return Comment
	 */
	public Comment getComment(String commentId) {
		return commentRepository.findById(commentId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.COMMENT_NOT_FOUND));
	}

	/**
	 * @param postId 게시글id
	 * @return Page<Comment>
	 */
	public Page<Comment> getComments(String postId, Pageable pageable) {
		Page<Comment> comments = commentRepository.findByPost_IdOrderByCreatedAt(postId, pageable);
		List<String> commentIds = comments.getContent().stream().map(Comment::getId).toList();

		if (!commentIds.isEmpty()) {
			List<ChildComment> allChildComments = childCommentReader.getChildCommentsByParentIds(commentIds);

			Map<String, List<ChildComment>> childCommentMap = allChildComments.stream()
				.collect(Collectors.groupingBy(child -> child.getParentComment().getId()));

			comments.forEach(comment -> comment
				.setChildCommentList(childCommentMap.getOrDefault(comment.getId(), Collections.emptyList())));
		}
		return comments;
	}

}
