package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.enums.CommentAdminStatus;
import net.causw.app.main.domain.community.comment.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class CommentAdminWriter {
	private final CommentRepository commentRepository;

	public void changeStatus(Comment comment, CommentAdminStatus status) {
		comment.changeAdminStatus(status);
		commentRepository.save(comment);
	}
}
