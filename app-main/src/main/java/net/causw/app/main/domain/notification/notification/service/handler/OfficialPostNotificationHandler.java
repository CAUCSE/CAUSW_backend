package net.causw.app.main.domain.notification.notification.service.handler;

import java.util.List;

import org.jsoup.Jsoup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
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
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.event.OfficialPostEvent;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.notification.notification.service.implementation.UserBoardSubscribeReader;
import net.causw.app.main.domain.notification.notification.util.NotificationTextUtil;
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
	@Transactional(propagation = Propagation.REQUIRES_NEW)
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

		// UserBoardSubscribe row가 없으면 기본 구독(true)으로 간주.
		// isSubscribed=false인 row가 명시적으로 존재하는 경우에만 알림 대상에서 제외.
		// ACTIVE + 미삭제 + readScope 조건을 만족하며 구독 거부하지 않은 유저 목록 조회
		BoardReadScope readScope = boardConfig.getReadScope();
		List<User> targets = userBoardSubscribeReader.findNotificationTargets(board.getId(), readScope);

		// 알림 발송
		// 푸시알림 제목: 게시판 이름
		// 푸시알림 내용: 공지글 내용 (최대 60자, 그 이상은 ...으로 표시) (크롤링은 추출된 제목)
		// 서비스 알림 제목: 공지글 내용 (최대 50자, 그 이상은 ...으로 표시) (크롤링은 추출된 제목)
		String pushTitle = board.getName();
		String rawPushBody;
		String serviceTitle;

		// 크롤링 공지의 경우 제목이 존재하므로 제목을 사용. 일반 게시글은 본문에서 텍스트만 추출하여 사용
		if (event.title() != null && !event.title().isBlank() && !"제목 없음".equals(event.title().trim())) {
			rawPushBody = event.title();
			serviceTitle = NotificationTextUtil.ellipsis(event.title(), NotificationTextUtil.SERVICE_TITLE_MAX_LENGTH);
		} else {
			String rawHtml = post.getContent() == null ? "" : post.getContent();
			String htmlWithNewlines = rawHtml.replace("</p>", "\n</p>");
			String sanitized = Jsoup.parse(htmlWithNewlines).text().trim();

			rawPushBody = sanitized;
			serviceTitle = NotificationTextUtil.ellipsis(sanitized, NotificationTextUtil.SERVICE_TITLE_MAX_LENGTH);
		}

		String pushBody = NotificationTextUtil.ellipsis(rawPushBody, NotificationTextUtil.PUSH_BODY_MAX_LENGTH);

		// 알림 발송자를 게시글 작성자로 설정하여 알림 저장
		Notification notification = notificationWriter.save(
			Notification.of(writer, serviceTitle, pushBody, NoticeType.OFFICIAL, post.getId(), board.getId()));

		notificationPushSender.sendToUsers(targets, pushTitle, pushBody);
		notificationWriter.saveLogs(targets, notification);
	}
}
