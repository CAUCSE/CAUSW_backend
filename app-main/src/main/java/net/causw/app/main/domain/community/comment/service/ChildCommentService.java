package net.causw.app.main.domain.community.comment.service;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.community.comment.service.dto.ChildCommentCreateCommand;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentResult;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentUpdateCommand;
import net.causw.app.main.domain.community.comment.service.dto.CommentCreateCommand;
import net.causw.app.main.domain.community.comment.service.dto.CommentResult;
import net.causw.app.main.domain.community.comment.service.dto.CommentUpdateCommand;

import lombok.RequiredArgsConstructor;

/**
 * 기존 대댓글 API 호환을 위해 통합 댓글 서비스를 감싸는 어댑터입니다.
 */
@Service
@RequiredArgsConstructor
public class ChildCommentService {

	private final CommentService commentService;

	public ChildCommentResult createChildComment(ChildCommentCreateCommand command) {
		CommentResult result = commentService.createComment(new CommentCreateCommand(
			command.content(),
			null,
			command.parentCommentId(),
			command.isAnonymous(),
			command.creatorId()));

		return toChildCommentResult(result);
	}

	public ChildCommentResult updateChildComment(ChildCommentUpdateCommand command) {
		CommentResult result = commentService.updateComment(new CommentUpdateCommand(
			command.childCommentId(),
			command.content(),
			command.updaterId()));

		return toChildCommentResult(result);
	}

	public ChildCommentResult deleteChildComment(String deleterId, String childCommentId) {
		return toChildCommentResult(commentService.deleteComment(deleterId, childCommentId));
	}

	public void likeChildComment(String userId, String childCommentId) {
		commentService.likeComment(userId, childCommentId);
	}

	public void cancelLikeChildComment(String userId, String childCommentId) {
		commentService.cancelLikeComment(userId, childCommentId);
	}

	private ChildCommentResult toChildCommentResult(CommentResult result) {
		return new ChildCommentResult(
			result.id(),
			result.content(),
			result.isDeleted(),
			result.createdAt(),
			result.updatedAt(),
			result.authorInfo(),
			result.isCommentLike(),
			result.numLike());
	}
}
