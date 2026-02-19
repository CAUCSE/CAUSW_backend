package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.notification.notification.entity.UserCommentSubscribe;
import net.causw.app.main.domain.notification.notification.repository.UserCommentSubscribeRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentSubscribeWriter {

	private final CommentReader commentReader;
	private final UserCommentSubscribeRepository userCommentSubscribeRepository;

	public void createCommentSubscribe(User user, String commentId) {
		Comment comment = commentReader.getComment(commentId);

		UserCommentSubscribe userCommentSubscribe = UserCommentSubscribe.of(user, comment, true);
		userCommentSubscribeRepository.save(userCommentSubscribe);
	}
}
