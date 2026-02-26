package net.causw.app.main.domain.community.comment.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.comment.api.v2.dto.request.CommentCreateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.request.CommentUpdateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentResponseDto;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.entity.LikeComment;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.CommentSubscribeWriter;
import net.causw.app.main.domain.community.comment.service.implementation.CommentWriter;
import net.causw.app.main.domain.community.comment.service.implementation.LikeChildCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.LikeCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.LikeCommentWriter;
import net.causw.app.main.domain.community.comment.util.CommentValidator;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.service.implementation.UserCommentSubscribeReader;
import net.causw.app.main.domain.notification.notification.service.v1.PostNotificationService;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;
import net.causw.app.main.shared.exception.errorcode.CommentErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final BoardConfigReader boardConfigReader;
	private final PostReader postReader;
	private final UserReader userReader;
	private final CommentReader commentReader;
	private final LikeCommentReader likeCommentReader;
	private final LikeChildCommentReader likeChildCommentReader;
	private final CommentWriter commentWriter;
	private final LikeCommentWriter likeCommentWriter;
	private final CommentSubscribeWriter commentSubscribeWriter;
	private final CommentValidator commentValidator;
	private final PostNotificationService postNotificationService;
	private final UserBlockEntityService userBlockEntityService;
	private final UserCommentSubscribeReader userCommentSubscribeReader;

	@Transactional
	public CommentResponseDto createComment(String creatorId, CommentCreateRequestDto commentCreateDto) {
		Post post = postReader.findById(commentCreateDto.postId());
		User creator = userReader.findUserById(creatorId);
		Comment comment = Comment.of(commentCreateDto.content(), false, commentCreateDto.isAnonymous(), creator,
			post);

		commentValidator.validateForCreate(creator, post, comment);

		commentWriter.save(comment);
		CommentResponseDto commentResponseDto = commentReader.getCommentDetail(comment, creator, post.getBoard());

		commentSubscribeWriter.createCommentSubscribe(creator, comment.getId());

		//comment가 달린 게시글의 구독자에게 전송
		postNotificationService.sendByPostIsSubscribed(post, comment);

		return commentResponseDto;
	}

	@Transactional(readOnly = true)
	public Page<CommentResponseDto> findAllComments(String userId, String postId, Pageable pageable) {
		Post post = postReader.findById(postId);
		User user = userReader.findUserById(userId);

		commentValidator.validateForFind(user, post);

		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());
		Set<String> blockedUserIds = userBlockEntityService.findBlockeeUserIdsByBlocker(user);
		Page<Comment> comments = commentReader.getComments(postId, pageable);

		// 부모 댓글 ID 리스트 추출
		List<String> commentIds = comments.getContent().stream().map(Comment::getId).toList();
		if (commentIds.isEmpty())
			return Page.empty(pageable); // 댓글이 없으면 조기 종료

		// 자식 댓글 ID 리스트 추출
		List<String> childCommentIds = comments.getContent().stream()
			.flatMap(c -> c.getChildCommentList().stream())
			.map(ChildComment::getId)
			.toList();

		// 부모 댓글 관련 데이터 조회
		Map<String, Long> commentLikeCounts = likeCommentReader.getCommentLikeCounts(commentIds);
		Set<String> likedCommentIds = likeCommentReader.getLikedCommentIds(user.getId(), commentIds);
		Set<String> subscribedCommentIds = userCommentSubscribeReader.getSubscribedCommentIds(user.getId(), commentIds);

		// 자식 댓글 관련 데이터 조회
		Map<String, Long> childCommentLikeCounts = childCommentIds.isEmpty()
			? Collections.emptyMap()
			: likeChildCommentReader.getChildCommentLikeCounts(childCommentIds);

		Set<String> likedChildCommentIds = childCommentIds.isEmpty()
			? Collections.emptySet()
			: likeChildCommentReader.getLikedChildCommentIds(user.getId(), childCommentIds);

		return comments.map(comment -> commentReader.getCommentListDetails(
			comment, user, blockedUserIds, boardAdminIds,
			commentLikeCounts, likedCommentIds, subscribedCommentIds,
			childCommentLikeCounts, likedChildCommentIds));
	}

	@Transactional
	public CommentResponseDto updateComment(
		String updaterId,
		String commentId,
		CommentUpdateRequestDto commentUpdateRequestDto) {
		User updater = userReader.findUserById(updaterId);
		Comment comment = commentReader.getComment(commentId);
		Post post = postReader.findById(comment.getPost().getId());

		commentValidator.validateForUpdate(updater, post, comment);

		comment.update(commentUpdateRequestDto.content());

		commentWriter.save(comment);

		return commentReader.getCommentDetail(comment, updater, post.getBoard());
	}

	@Transactional
	public CommentResponseDto deleteComment(String deleterId, String commentId) {
		User deleter = userReader.findUserById(deleterId);
		Comment comment = commentReader.getComment(commentId);
		Post post = postReader.findById(comment.getPost().getId());

		commentValidator.validateForDelete(deleter, post, comment);

		comment.delete();

		commentWriter.save(comment);

		return commentReader.getCommentDetail(comment, deleter, post.getBoard());
	}

	@Transactional
	public void likeComment(String userId, String commentId) {
		User user = userReader.findUserById(userId);
		Comment comment = commentReader.getComment(commentId);

		commentValidator.validateWriterNotDeleted(comment);

		if (likeCommentReader.isCommentLiked(user, commentId)) {
			throw CommentErrorCode.COMMENT_ALREADY_LIKED.toBaseException();
		}

		LikeComment likeComment = LikeComment.of(comment, user);
		likeCommentWriter.save(likeComment);
	}

	@Transactional
	public void cancelLikeComment(String userId, String commentId) {
		User user = userReader.findUserById(userId);
		Comment comment = commentReader.getComment(commentId);

		commentValidator.validateWriterNotDeleted(comment);

		if (!likeCommentReader.isCommentLiked(user, commentId)) {
			throw CommentErrorCode.COMMENT_NOT_LIKE.toBaseException();
		}

		likeCommentWriter.delete(commentId, user.getId());
	}

}
