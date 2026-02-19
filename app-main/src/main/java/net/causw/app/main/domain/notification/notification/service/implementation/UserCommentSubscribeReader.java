package net.causw.app.main.domain.notification.notification.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.notification.notification.entity.UserCommentSubscribe;
import net.causw.app.main.domain.notification.notification.repository.UserCommentSubscribeRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserCommentSubscribeReader {

	private final UserCommentSubscribeRepository userCommentSubscribeRepository;

	public Boolean isCommentSubscribed(User user, Comment comment) {
		return userCommentSubscribeRepository.findByUserAndComment(user, comment)
			.map(UserCommentSubscribe::getIsSubscribed)
			.orElse(false);
	}

}
