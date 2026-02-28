package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.notification.notification.entity.UserCommentSubscribe;
import net.causw.app.main.domain.notification.notification.repository.UserCommentSubscribeRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

/**
 * 댓글 구독 엔티티 저장을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class CommentSubscribeWriter {

	private final CommentReader commentReader;
	private final UserCommentSubscribeRepository userCommentSubscribeRepository;

	/**
	 * 유저가 댓글을 구독하도록 구독 정보를 생성·저장합니다.
	 *
	 * <p>댓글 생성 직후 작성자가 자동으로 구독 상태가 되도록 호출됩니다.</p>
	 *
	 * @param user      구독할 유저
	 * @param commentId 구독할 댓글 ID
	 */
	public void createCommentSubscribe(User user, String commentId) {
		Comment comment = commentReader.getComment(commentId);

		UserCommentSubscribe userCommentSubscribe = UserCommentSubscribe.of(user, comment, true);
		userCommentSubscribeRepository.save(userCommentSubscribe);
	}
}
