package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.UserCommentSubscribe;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.service.v2.event.CommentChildCommentCreatedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;
import net.causw.app.main.shared.entity.BaseEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentNotificationHandler {

	private final UserCommentSubscribeReader userCommentSubscribeReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final UserBlockEntityService userBlockEntityService;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handle(CommentChildCommentCreatedEvent event) {
		Comment comment = event.comment();
		ChildComment childComment = event.childComment();
		User commentWriter = comment.getWriter();
		User childCommentWriter = childComment.getWriter();

		Set<String> blockerUserIds = getBlockerUserIds(commentWriter, childCommentWriter);
		List<UserCommentSubscribe> subscribers = userCommentSubscribeReader.findForNotification(comment,
			blockerUserIds);

		String title = comment.getContent();
		String body = String.format("새 대댓글 : %s", childComment.getContent());

		Notification notification = notificationWriter.save(
			Notification.of(childCommentWriter, title, body, NoticeType.COMMENT,
				comment.getPost().getId(), comment.getPost().getBoard().getId()));

		subscribers.stream()
			.map(UserCommentSubscribe::getUser)
			.forEach(user -> {
				notificationPushSender.sendToUser(user, title, body);
				notificationWriter.saveLog(user, notification);
			});
	}

	private Set<String> getBlockerUserIds(User commentWriter, User childCommentWriter) {
		Set<String> blockeeUserIds = Stream.of(commentWriter, childCommentWriter)
			.filter(Objects::nonNull)
			.map(BaseEntity::getId)
			.collect(Collectors.toSet());
		return userBlockEntityService.findBlockerUserIdsByUserIds(blockeeUserIds);
	}
}
