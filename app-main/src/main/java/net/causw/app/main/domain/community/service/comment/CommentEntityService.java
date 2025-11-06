package net.causw.app.main.domain.community.service.comment;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.community.entity.comment.Comment;
import net.causw.app.main.domain.community.repository.comment.CommentRepository;
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
