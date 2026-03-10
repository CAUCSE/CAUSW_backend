package net.causw.app.main.domain.community.comment.service.v2.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.repository.ChildCommentRepository;
import net.causw.app.main.shared.exception.errorcode.ChildCommentErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChildCommentReader {

	private final ChildCommentRepository childCommentRepository;

	public ChildComment findByIdAndNotDeleted(String childCommentId) {
		return childCommentRepository.findByIdAndIsDeletedFalse(childCommentId)
			.orElseThrow(ChildCommentErrorCode.CHILD_COMMENT_NOT_FOUND::toBaseException);
	}
}
