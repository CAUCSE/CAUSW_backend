package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.service.v2.event.BoardPostCreatedEvent;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardNotificationHandler {

	private final UserBoardSubscribeReader userBoardSubscribeReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final UserBlockEntityService userBlockEntityService;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handle(BoardPostCreatedEvent event) {
		Board board = event.board();
		Post post = event.post();
		User writer = post.getWriter();

		Set<String> blockerUserIds = userBlockEntityService.findBlockerUserIdsByBlockee(writer);
		List<UserBoardSubscribe> subscribers = userBoardSubscribeReader.findForNotification(board, blockerUserIds);

		List<UserBoardSubscribe> filtered = subscribers.stream()
			.filter(subscribe -> board.getIsAlumni()
				|| subscribe.getUser().getAcademicStatus() != AcademicStatus.GRADUATED)
			.toList();

		String title = board.getName();
		String body = String.format("새 게시글 : %s", post.getTitle());

		Notification notification = notificationWriter.save(
			Notification.of(writer, title, body, NoticeType.BOARD, post.getId(), board.getId()));

		filtered.stream()
			.map(UserBoardSubscribe::getUser)
			.forEach(user -> {
				notificationPushSender.sendToUser(user, title, body);
				notificationWriter.saveLog(user, notification);
			});
	}
}
