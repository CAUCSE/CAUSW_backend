package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.repository.ChildCommentRepository;
import net.causw.app.main.shared.exception.errorcode.ChildCommentErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 대댓글 엔티티 조회를 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class ChildCommentReader {

	private final ChildCommentRepository childCommentRepository;

	/**
	 * ID로 대댓글을 조회합니다. 존재하지 않으면 예외를 발생시킵니다.
	 *
	 * @param childCommentId 조회할 대댓글 ID
	 * @return 조회된 {@link ChildComment} 엔티티
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 대댓글이 존재하지 않는 경우
	 */
	public ChildComment findById(String childCommentId) {
		return childCommentRepository.findById(childCommentId).orElseThrow(
			ChildCommentErrorCode.CHILD_COMMENT_NOT_FOUND::toBaseException);
	}

	/**
	 * 부모 댓글 ID 목록에 속한 대댓글을 일괄 조회합니다.
	 *
	 * <p>{@link CommentReader#getComments} 에서 N+1 문제를 방지하기 위해 호출됩니다.</p>
	 *
	 * @param parentCommentIds 부모 댓글 ID 목록
	 * @return 해당 부모 댓글들에 속한 대댓글 목록
	 */
	public List<ChildComment> getChildCommentsByParentIds(List<String> parentCommentIds) {
		return childCommentRepository.findChildCommentsByParentCommentIds(parentCommentIds);
	}

}
