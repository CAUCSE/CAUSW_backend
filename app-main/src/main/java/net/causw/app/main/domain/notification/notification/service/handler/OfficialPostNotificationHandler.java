package net.causw.app.main.domain.notification.notification.service.handler;

import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.event.OfficialPostEvent;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.notification.notification.service.implementation.UserBoardSubscribeReader;
import net.causw.app.main.domain.notification.notification.util.NotificationTextUtil;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OfficialPostNotificationHandler {

	private final UserBoardSubscribeReader userBoardSubscribeReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final BoardConfigReader boardConfigReader;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handle(OfficialPostEvent event) {
		Board board = event.board();
		Post post = event.post();
		User writer = post.getWriter();

		BoardConfig boardConfig = boardConfigReader.getByBoardId(board.getId());
		if (!boardConfig.isNotice() || boardConfig.getVisibility() != BoardVisibility.VISIBLE) {
			return;
		}

		List<UserBoardSubscribe> subscribers = userBoardSubscribeReader.findForNotification(board, Set.of());

		BoardReadScope readScope = boardConfig.getReadScope();
		List<User> targets = subscribers.stream()
			.map(UserBoardSubscribe::getUser)
			.filter(user -> isInReadScope(user.getAcademicStatus(), readScope))
			.toList();

		String sanitizedContent = NotificationTextUtil.sanitize(post.getContent());
		String pushTitle = board.getName();
		String pushBody = NotificationTextUtil.ellipsis(sanitizedContent, NotificationTextUtil.PUSH_BODY_MAX_LENGTH);
		String serviceTitle = NotificationTextUtil.ellipsis(sanitizedContent,
			NotificationTextUtil.SERVICE_TITLE_MAX_LENGTH);

		Notification notification = notificationWriter.save(
			Notification.of(writer, serviceTitle, pushBody, NoticeType.BOARD, post.getId(), board.getId()));

		notificationPushSender.sendToUsers(targets, pushTitle, pushBody);
		notificationWriter.saveLogs(targets, notification);
	}

	private static boolean isInReadScope(AcademicStatus status, BoardReadScope readScope) {
		return switch (readScope) {
			case ENROLLED -> status != AcademicStatus.GRADUATED;
			case GRADUATED -> status == AcademicStatus.GRADUATED;
			case BOTH -> true;
		};
	}
}
