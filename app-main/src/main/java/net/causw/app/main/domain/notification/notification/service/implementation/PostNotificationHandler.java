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

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.UserPostSubscribe;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.service.v2.event.PostCommentCreatedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;
import net.causw.app.main.shared.entity.BaseEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostNotificationHandler {

	private final UserPostSubscribeReader userPostSubscribeReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final UserBlockEntityService userBlockEntityService;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handle(PostCommentCreatedEvent event) {
		Post post = event.post();
		Comment comment = event.comment();
		User postWriter = post.getWriter();
		User commentWriter = comment.getWriter();

		Set<String> blockerUserIds = getBlockerUserIds(postWriter, commentWriter);
		List<UserPostSubscribe> subscribers = userPostSubscribeReader.findForNotification(post, blockerUserIds);

		String title = post.getTitle();
		String body = String.format("새 댓글 : %s", comment.getContent());

		Notification notification = notificationWriter.save(
			Notification.of(commentWriter, title, body, NoticeType.POST, post.getId(), post.getBoard().getId()));

		subscribers.stream()
			.map(UserPostSubscribe::getUser)
			.forEach(user -> {
				notificationPushSender.sendToUser(user, title, body);
				notificationWriter.saveLog(user, notification);
			});
	}

	private Set<String> getBlockerUserIds(User postWriter, User commentWriter) {
		Set<String> blockeeUserIds = Stream.of(postWriter, commentWriter)
			.filter(Objects::nonNull)
			.map(BaseEntity::getId)
			.collect(Collectors.toSet());
		return userBlockEntityService.findBlockerUserIdsByUserIds(blockeeUserIds);
	}
}
