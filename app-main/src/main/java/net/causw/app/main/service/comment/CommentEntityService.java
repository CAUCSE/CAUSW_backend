package net.causw.app.main.service.comment;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.repository.comment.CommentRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentEntityService {

	private final CommentRepository commentRepository;

	public Comment findByIdNotDeleted(String commentId) {

		return commentRepository.findByIdAndIsDeletedFalse(commentId).orElseThrow(() ->
			new NotFoundException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.COMMENT_NOT_FOUND
			)
		);
	}
}
