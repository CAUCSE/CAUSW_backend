package net.causw.app.main.domain.community.comment.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.api.v2.dto.request.CommentCreateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.request.CommentUpdateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentResponseDto;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.entity.LikeComment;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.CommentSubscribeWriter;
import net.causw.app.main.domain.community.comment.service.implementation.CommentWriter;
import net.causw.app.main.domain.community.comment.service.implementation.LikeCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.LikeCommentWriter;
import net.causw.app.main.domain.community.comment.util.CommentValidator;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.service.v1.PostNotificationService;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final PostReader postReader;
	private final UserReader userReader;
	private final CommentReader commentReader;
	private final LikeCommentReader likeCommentReader;
	private final CommentWriter commentWriter;
	private final LikeCommentWriter likeCommentWriter;
	private final CommentSubscribeWriter commentSubscribeWriter;
	private final CommentValidator commentValidator;
	private final PostNotificationService postNotificationService;
	private final UserBlockEntityService userBlockEntityService;

	@Transactional
	public CommentResponseDto createComment(String creatorId, CommentCreateRequestDto commentCreateDto) {
		Post post = postReader.getPost(commentCreateDto.postId());
		User creator = userReader.getUser(creatorId);
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
	public Page<CommentResponseDto> findAllComments(String userId, String postId, Integer pageNum) {
		Post post = postReader.getPost(postId);
		User user = userReader.getUser(userId);

		commentValidator.validateForFind(user, post);

		Set<String> blockedUserIds = userBlockEntityService.findBlockeeUserIdsByBlocker(user);

		Page<Comment> comments = commentReader.getComments(postId, pageNum);

		return comments
			.map(comment -> commentReader.getCommentDetailForList(comment, user, post.getBoard(), blockedUserIds));
	}

	@Transactional
	public CommentResponseDto updateComment(
		String updaterId,
		String commentId,
		CommentUpdateRequestDto commentUpdateRequestDto) {
		User updater = userReader.getUser(updaterId);
		Comment comment = commentReader.getComment(commentId);
		Post post = postReader.getPost(comment.getPost().getId());

		commentValidator.validateForUpdate(updater, post, comment);

		comment.update(commentUpdateRequestDto.content());

		commentWriter.save(comment);

		return commentReader.getCommentDetail(comment, updater, post.getBoard());
	}

	@Transactional
	public CommentResponseDto deleteComment(String deleterId, String commentId) {
		User deleter = userReader.getUser(deleterId);
		Comment comment = commentReader.getComment(commentId);
		Post post = postReader.getPost(comment.getPost().getId());

		commentValidator.validateForDelete(deleter, post, comment);

		comment.delete();

		commentWriter.save(comment);

		return commentReader.getCommentDetail(comment, deleter, post.getBoard());
	}

	@Transactional
	public void likeComment(String userId, String commentId) {
		User user = userReader.getUser(userId);
		Comment comment = commentReader.getComment(commentId);

		commentValidator.validateWriterNotDeleted(comment);

		if (likeCommentReader.isCommentLiked(user, commentId)) {
			throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.COMMENT_ALREADY_LIKED);
		}

		LikeComment likeComment = LikeComment.of(comment, user);
		likeCommentWriter.save(likeComment);
	}

	@Transactional
	public void cancelLikeComment(String userId, String commentId) {
		User user = userReader.getUser(userId);
		Comment comment = commentReader.getComment(commentId);

		commentValidator.validateWriterNotDeleted(comment);

		if (!likeCommentReader.isCommentLiked(user, commentId)) {
			throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.COMMENT_NOT_LIKED);
		}

		likeCommentWriter.delete(commentId, user.getId());
	}

}
