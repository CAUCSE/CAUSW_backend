package net.causw.app.main.domain.community.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.enums.CommentAdminStatus;
import net.causw.app.main.domain.community.comment.service.dto.CommentAdminResult;
import net.causw.app.main.domain.community.comment.service.implementation.CommentAdminWriter;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentAdminService {
	private final CommentReader commentReader;
	private final CommentAdminWriter commentAdminWriter;
	private final PostReader postReader;

	public Page<CommentAdminResult> getComments(String postId, Pageable pageable) {
		postReader.findById(postId);
		return commentReader.getComments(postId, pageable).map(CommentAdminResult::from);
	}

	@Transactional
	public void changeStatus(String commentId, CommentAdminStatus status) {
		Comment comment = commentReader.getCommentIncludeDeleted(commentId);
		commentAdminWriter.changeStatus(comment, status);
	}
}
