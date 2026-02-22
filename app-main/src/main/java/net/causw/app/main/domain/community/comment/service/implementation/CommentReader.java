package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentResponseDto;
import net.causw.app.main.domain.community.comment.api.v2.mapper.CommentResponseDtoMapper;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.repository.CommentRepository;
import net.causw.app.main.domain.notification.notification.service.implementation.UserCommentSubscribeReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.StatusPolicy;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentReader {

	private final LikeCommentReader likeCommentReader;
	private final ChildCommentReader childCommentReader;
	private final UserCommentSubscribeReader userCommentSubscribeReader;
	private final CommentResponseDtoMapper commentResponseDtoMapper;
	private final CommentRepository commentRepository;
	private final PageableFactory pageableFactory;

	/**
	 * @param comment 댓글
	 * @param user 조회자
	 * @param board 게시판
	 * @return CommentResponseDto
	 */
	public CommentResponseDto getCommentDetail(
		Comment comment, User user, Board board) {

		return commentResponseDtoMapper.toCommentResponseDto(
			comment,
			childCommentReader.getNumOfChildComments(comment.getId()),
			likeCommentReader.getNumOfCommentLikes(comment.getId()),
			likeCommentReader.isCommentLiked(user, comment.getId()),
			StatusPolicy.isCommentOwner(comment, user),
			comment.getChildCommentList().stream()
				.map(childComment -> childCommentReader.getChildCommentDetail(childComment, user, board))
				.collect(Collectors.toList()),
			StatusPolicy.isUpdatable(comment, user),
			StatusPolicy.isDeletable(comment, user, board),
			userCommentSubscribeReader.isCommentSubscribed(user, comment),
			false);
	}

	/**
	 * @param comment 댓글
	 * @param user 조회자
	 * @param board 게시판
	 * @return CommentResponseDto
	 */
	public CommentResponseDto getCommentDetailForList(
		Comment comment, User user, Board board, Set<String> blockedUserIds) {

		boolean isBlockedContent = blockedUserIds.contains(comment.getWriter().getId());

		return commentResponseDtoMapper.toCommentResponseDto(
			comment,
			childCommentReader.getNumOfChildComments(comment.getId()),
			likeCommentReader.getNumOfCommentLikes(comment.getId()),
			likeCommentReader.isCommentLiked(user, comment.getId()),
			StatusPolicy.isCommentOwner(comment, user),
			comment.getChildCommentList().stream()
				.map(childComment -> childCommentReader.getChildCommentDetailForList(childComment, user, board,
					blockedUserIds))
				.collect(Collectors.toList()),
			StatusPolicy.isUpdatable(comment, user),
			StatusPolicy.isDeletable(comment, user, board),
			userCommentSubscribeReader.isCommentSubscribed(user, comment),
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
		comments.forEach(
			comment -> comment.setChildCommentList(childCommentReader.getChildComments(comment.getId())));

		return comments;
	}

}
