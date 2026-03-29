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
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
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

	private final BoardReader boardReader;
	private final PostReader postReader;
	private final UserBoardSubscribeReader userBoardSubscribeReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final BoardConfigReader boardConfigReader;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handle(OfficialPostEvent event) {
		// ID로 게시판·게시글 조회
		Board board = boardReader.getById(event.boardId());
		Post post = postReader.findById(event.postId());
		User writer = post.getWriter();

		// 공지글이 아닌 경우, 게시판이 공개되지 않은 경우 알림을 보내지 않음
		BoardConfig boardConfig = boardConfigReader.getByBoardId(board.getId());
		if (!boardConfig.isNotice() || boardConfig.getVisibility() != BoardVisibility.VISIBLE) {
			return;
		}

		// 게시판 구독자 중 읽기 범위에 해당하는 유저들에게 알림 발송
		List<UserBoardSubscribe> subscribers = userBoardSubscribeReader.findForNotification(board, Set.of());

		// 게시판 읽기 범위에 해당하는 유저 필터링
		BoardReadScope readScope = boardConfig.getReadScope();
		List<User> targets = subscribers.stream()
			.map(UserBoardSubscribe::getUser)
			.filter(user -> isInReadScope(user.getAcademicStatus(), readScope))
			.toList();

		// 알림 발송
		// 푸시알림 제목: 게시판 이름
		// 푸시알림 내용: 공지글 내용 (최대 60자, 그 이상은 ...으로 표시)
		// 서비스 알림 제목: 공지글 내용 (최대 20자, 그 이상은 ...으로 표시)
		String sanitizedContent = NotificationTextUtil.sanitize(post.getContent());
		String pushTitle = board.getName();
		String pushBody = NotificationTextUtil.ellipsis(sanitizedContent, NotificationTextUtil.PUSH_BODY_MAX_LENGTH);
		String serviceTitle = NotificationTextUtil.ellipsis(sanitizedContent,
			NotificationTextUtil.SERVICE_TITLE_MAX_LENGTH);

		// 알림 발송자를 게시글 작성자로 설정하여 알림 저장
		Notification notification = notificationWriter.save(
			Notification.of(writer, serviceTitle, pushBody, NoticeType.OFFICIAL, post.getId(), board.getId()));

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
