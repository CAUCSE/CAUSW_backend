package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.repository.ChildCommentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChildCommentWriter {
	private final ChildCommentRepository childCommentRepository;

	public void save(ChildComment childComment) {
		childCommentRepository.save(childComment);
	}
}
