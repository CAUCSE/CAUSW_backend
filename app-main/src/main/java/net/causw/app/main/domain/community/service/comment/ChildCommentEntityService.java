package net.causw.app.main.domain.community.service.comment;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.community.entity.comment.ChildComment;
import net.causw.app.main.domain.community.repository.comment.ChildCommentRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChildCommentEntityService {

	private final ChildCommentRepository childCommentRepository;

	public ChildComment findByIdNotDeleted(String childCommentId) {

		return childCommentRepository.findByIdAndIsDeletedFalse(childCommentId)
			.orElseThrow(() ->
				new NotFoundException(
					ErrorCode.ROW_DOES_NOT_EXIST,
					MessageUtil.CHILD_COMMENT_NOT_FOUND
				)
			);
	}
}
