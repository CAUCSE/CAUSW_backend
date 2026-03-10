package net.causw.app.main.domain.community.comment.service.v2.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.repository.CommentRepository;
import net.causw.app.main.shared.exception.errorcode.CommentErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentReader {

	private final CommentRepository commentRepository;

	public Comment findByIdAndNotDeleted(String commentId) {
		return commentRepository.findByIdAndIsDeletedFalse(commentId)
			.orElseThrow(CommentErrorCode.COMMENT_NOT_FOUND::toBaseException);
	}
}
