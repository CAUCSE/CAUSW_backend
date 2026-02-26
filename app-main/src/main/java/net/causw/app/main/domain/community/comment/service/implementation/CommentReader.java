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
import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentResponseDto;
import net.causw.app.main.domain.community.comment.api.v2.mapper.CommentResponseDtoMapper;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.repository.CommentRepository;
import net.causw.app.main.domain.notification.notification.service.implementation.UserCommentSubscribeReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.StatusPolicy;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentReader {

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

		// 자식 댓글 ID 리스트 추출
		List<String> childCommentIds = comment.getChildCommentList().stream()
			.map(ChildComment::getId)
			.toList();

		// 자식 댓글용 데이터 조회
		Map<String, Long> childCommentLikeCounts = childCommentIds.isEmpty()
			? Collections.emptyMap()
			: likeChildCommentReader.getChildCommentLikeCounts(childCommentIds);

		Set<String> likedChildCommentIds = childCommentIds.isEmpty()
			? Collections.emptySet()
			: likeChildCommentReader.getLikedChildCommentIds(user.getId(), childCommentIds);

		List<ChildCommentResponseDto> childCommentList = comment.getChildCommentList().stream()
			.map(childComment -> childCommentReader.getChildCommentListDetails(
				childComment, user, board, Set.of(),
				childCommentLikeCounts.getOrDefault(childComment.getId(), 0L),
				likedChildCommentIds.contains(childComment.getId())

			)).toList();

		return commentResponseDtoMapper.toCommentResponseDto(
			comment,
			(long)comment.getChildCommentList().size(),
			likeCommentReader.getNumOfCommentLikes(comment.getId()),
			likeCommentReader.isCommentLiked(user, comment.getId()),
			StatusPolicy.isCommentOwner(comment, user),
			childCommentList,
			StatusPolicy.isUpdatable(comment, user),
			StatusPolicy.isDeletable(comment, user, board),
			userCommentSubscribeReader.isCommentSubscribed(user, comment),
			false);
	}

	/**
	 * @param comment 댓글
	 * @param user 조회자
	 * @param board 게시판
	 * @param blockedUserIds 차단된 유저 ID 목록
	 * @param commentLikeCounts 미리 조회한 부모 댓글들의 좋아요 수 Map
	 * @param likedCommentIds 현재 유저가 좋아요를 누른 부모 댓글 ID Set
	 * @param subscribedCommentIds 현재 유저가 구독한 부모 댓글 ID Set
	 * @param childCommentLikeCounts 미리 조회한 자식 댓글들의 좋아요 수 Map
	 * @param likedChildCommentIds 현재 유저가 좋아요를 누른 자식 댓글 ID Set
	 * @return CommentResponseDto
	 */
	public CommentResponseDto getCommentListDetails(
		Comment comment, User user, Board board, Set<String> blockedUserIds,
		Map<String, Long> commentLikeCounts,
		Set<String> likedCommentIds,
		Set<String> subscribedCommentIds,
		Map<String, Long> childCommentLikeCounts,
		Set<String> likedChildCommentIds) {

		// 작성자 차단 여부 확인
		boolean isBlockedContent = blockedUserIds.contains(comment.getWriter().getId());

		List<ChildCommentResponseDto> childCommetList = comment.getChildCommentList().stream()
			.map(childComment -> childCommentReader.getChildCommentListDetails(
				childComment, user, board, blockedUserIds,
				childCommentLikeCounts.getOrDefault(childComment.getId(), 0L), // 자식 댓글 좋아요 수
				likedChildCommentIds.contains(childComment.getId()) // 자식 댓글 좋아요 여부
			)).toList();

		return commentResponseDtoMapper.toCommentResponseDto(
			comment,
			(long)comment.getChildCommentList().size(),
			commentLikeCounts.getOrDefault(comment.getId(), 0L),
			likedCommentIds.contains(comment.getId()),
			StatusPolicy.isCommentOwner(comment, user),
			childCommetList,
			StatusPolicy.isUpdatable(comment, user),
			StatusPolicy.isDeletable(comment, user, board),
			subscribedCommentIds.contains(comment.getId()),
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
