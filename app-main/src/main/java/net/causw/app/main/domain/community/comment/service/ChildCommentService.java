package net.causw.app.main.domain.community.comment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.api.v2.dto.request.ChildCommentCreateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.request.ChildCommentUpdateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.entity.LikeChildComment;
import net.causw.app.main.domain.community.comment.service.implementation.ChildCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.ChildCommentWriter;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.LikeChildCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.LikeChildCommentWriter;
import net.causw.app.main.domain.community.comment.util.ChildCommentValidator;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.service.v1.CommentNotificationService;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.shared.exception.errorcode.ChildCommentErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChildCommentService {

	private final UserReader userReader;
	private final CommentReader commentReader;
	private final ChildCommentReader childCommentReader;
	private final LikeChildCommentReader likeChildCommentReader;
	private final PostReader postReader;
	private final ChildCommentWriter childCommentWriter;
	private final LikeChildCommentWriter likeChildCommentWriter;
	private final ChildCommentValidator childCommentValidator;
	private final CommentNotificationService commentNotificationService;

	@Transactional
	public ChildCommentResponseDto createChildComment(String creatorId,
		ChildCommentCreateRequestDto childCommentCreateRequestDto) {

		User creator = userReader.findUserById(creatorId);
		Comment parentComment = commentReader.getComment(childCommentCreateRequestDto.parentCommentId());
		Post post = postReader.findById(parentComment.getPost().getId());
		ChildComment childComment = ChildComment.of(
			childCommentCreateRequestDto.content(),
			false,
			childCommentCreateRequestDto.isAnonymous(),
			creator,
			parentComment);

		childCommentValidator.validateForCreate(creator, post, parentComment, childComment);

		childCommentWriter.save(childComment);
		ChildCommentResponseDto childCommentResponseDto = childCommentReader.getChildCommentDetail(
			childComment,
			creator,
			post.getBoard());

		commentNotificationService.sendByCommentIsSubscribed(parentComment, childComment);

		return childCommentResponseDto;
	}

	@Transactional
	public ChildCommentResponseDto updateChildComment(
		String updaterId,
		String childCommentId,
		ChildCommentUpdateRequestDto childCommentUpdateRequestDto) {

		User updater = userReader.findUserById(updaterId);
		ChildComment childComment = childCommentReader.findById(childCommentId);
		Post post = postReader.findById(childComment.getParentComment().getPost().getId());
		childComment.update(childCommentUpdateRequestDto.content());

		childCommentValidator.validateForUpdate(updater, post, childComment);

		childCommentWriter.save(childComment);

		return childCommentReader.getChildCommentDetail(
			childComment,
			updater,
			post.getBoard());
	}

	@Transactional
	public ChildCommentResponseDto deleteChildComment(String deleterId, String childCommentId) {
		User deleter = userReader.findUserById(deleterId);
		ChildComment childComment = childCommentReader.findById(childCommentId);
		Post post = postReader.findById(childComment.getParentComment().getPost().getId());

		childCommentValidator.validateForDelete(deleter, post, childComment);

		childComment.delete();

		childCommentWriter.save(childComment);

		return childCommentReader.getChildCommentDetail(
			childComment,
			deleter,
			post.getBoard());
	}

	@Transactional
	public void likeChildComment(String userId, String childCommentId) {
		User user = userReader.findUserById(userId);
		ChildComment childComment = childCommentReader.findById(childCommentId);

		childCommentValidator.validateWriterNotDeleted(childComment);

		if (likeChildCommentReader.isChildCommentLiked(user, childCommentId)) {
			throw ChildCommentErrorCode.CHILD_COMMENT_ALREADY_LIKED.toBaseException();
		}

		LikeChildComment likeChildComment = LikeChildComment.of(childComment, user);
		likeChildCommentWriter.save(likeChildComment);
	}

	@Transactional
	public void cancelLikeChildComment(String userId, String childCommentId) {
		User user = userReader.findUserById(userId);
		ChildComment childComment = childCommentReader.findById(childCommentId);

		childCommentValidator.validateWriterNotDeleted(childComment);

		if (!likeChildCommentReader.isChildCommentLiked(user, childCommentId)) {
			throw ChildCommentErrorCode.CHILD_COMMENT_NOT_LIKE.toBaseException();
		}

		likeChildCommentWriter.delete(childCommentId, user.getId());
	}
}
