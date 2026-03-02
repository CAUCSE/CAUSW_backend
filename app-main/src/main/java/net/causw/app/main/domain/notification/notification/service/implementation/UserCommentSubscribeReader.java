package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.notification.notification.entity.UserCommentSubscribe;
import net.causw.app.main.domain.notification.notification.repository.UserCommentSubscribeRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCommentSubscribeReader {

	private final UserCommentSubscribeRepository userCommentSubscribeRepository;

	public Boolean isCommentSubscribed(User user, Comment comment) {
		return userCommentSubscribeRepository.findByUserAndComment(user, comment)
			.map(UserCommentSubscribe::getIsSubscribed)
			.orElse(false);
	}

	/**
	 * @param userId 현재 조회하는 유저의 ID
	 * @param commentIds 부모 댓글 ID 리스트
	 * @return Set<댓글ID> (유저가 알림/구독 설정한 댓글들의 ID 집합)
	 */
	public Set<String> getSubscribedCommentIds(String userId, List<String> commentIds) {
		if (commentIds == null || commentIds.isEmpty()) {
			return Collections.emptySet();
		}
		return userCommentSubscribeRepository.findSubscribedCommentIdsByUserIdAndCommentIds(userId, commentIds);
	}

	public List<UserCommentSubscribe> findForNotification(Comment comment, Set<String> blockerUserIds) {
		return userCommentSubscribeRepository.findByCommentAndIsSubscribedTrueExcludingBlockerUsers(comment,
			blockerUserIds);
	}

}
