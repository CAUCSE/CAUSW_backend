package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.repository.ChildCommentRepository;

import lombok.RequiredArgsConstructor;

/**
 * 대댓글 엔티티 저장을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class ChildCommentWriter {

	private final ChildCommentRepository childCommentRepository;

	/**
	 * 대댓글을 저장(생성 또는 수정)합니다.
	 *
	 * @param childComment 저장할 {@link ChildComment} 엔티티
	 */
	public void save(ChildComment childComment) {
		childCommentRepository.save(childComment);
	}
}
