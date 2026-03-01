package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

/**
 * 댓글 엔티티 저장을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class CommentWriter {

	private final CommentRepository commentRepository;

	/**
	 * 댓글을 저장(생성 또는 수정)합니다.
	 *
	 * @param comment 저장할 {@link Comment} 엔티티
	 */
	public void save(Comment comment) {
		commentRepository.save(comment);
	}

}
