package net.causw.app.main.domain.notification.notification.service.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.event.CommunityPostEvent;
import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.notification.notification.service.implementation.UserBoardSubscribeReader;
import net.causw.app.main.domain.notification.notification.util.NotificationTextUtil;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommunityPostNotificationListener {

	private final BoardReader boardReader;
	private final PostReader postReader;
	private final BoardConfigReader boardConfigReader;
	private final UserBoardSubscribeReader userBoardSubscribeReader;
	private final BlockReader blockReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;

	/**
	 * [소통 게시판 새 글 알림]
	 * 소통 게시판에 글이 작성되었을 때, 해당 게시판 알림을 켠 유저에게 알림을 발송하는 핸들러
	 * - 소식 게시판, 시스템 공지 게시판, 공개되지 않은 게시판은 알림을 보내지 않음
	 * - 게시판 알림을 명시적으로 켠 유저만 대상 (기본값 OFF)
	 * - 게시글 작성자 본인, 작성자와 차단 관계인 유저는 제외
	 * @param event 소통 게시판 글 작성 이벤트 객체
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(CommunityPostEvent event) {
		// ID로 게시판·게시글 조회
		Board board = boardReader.getById(event.boardId());
		Post post = postReader.findById(event.postId());
		User writer = post.getWriter();

		BoardConfig boardConfig = boardConfigReader.getByBoardId(board.getId());
		if (boardConfig.isNotice() || boardConfig.isSystemNotice()
			|| boardConfig.getVisibility() != BoardVisibility.VISIBLE) {
			return;
		}

		// 작성자 본인과 차단 관계(작성자를 차단한 유저, 작성자가 차단한 유저)는 제외
		Set<String> excludedUserIds = new HashSet<>();
		excludedUserIds.add(writer.getId());
		excludedUserIds.addAll(blockReader.findBlockerUserIdsByBlockee(writer));
		excludedUserIds.addAll(blockReader.findBlockeeUserIdsByBlocker(writer));

		Set<String> boardAdminIds = boardConfigReader.getAdminIdSetMapByBoardIds(List.of(board.getId()))
			.getOrDefault(board.getId(), Set.of());
		List<User> targets = userBoardSubscribeReader.findCommunityNotificationTargets(
			board.getId(), boardConfig, boardAdminIds, excludedUserIds);
		if (targets.isEmpty()) {
			return;
		}

		// 푸시알림 제목: 게시판 이름
		// 푸시알림 내용: 게시글 제목 (없으면 본문 텍스트) (최대 60자, 그 이상은 ...으로 표시)
		// 서비스 알림 제목: 게시글 제목 (없으면 본문 텍스트) (최대 50자, 그 이상은 ...으로 표시)
		String pushTitle = board.getName();
		String rawText = resolvePostText(post);
		String pushBody = NotificationTextUtil.ellipsis(rawText, NotificationTextUtil.PUSH_BODY_MAX_LENGTH);
		String serviceTitle = NotificationTextUtil.ellipsis(rawText, NotificationTextUtil.SERVICE_TITLE_MAX_LENGTH);

		PushNotificationData pushData = new PushNotificationData(null, NoticeType.COMMUNITY, post.getId(),
			board.getId());

		Notification notification = notificationWriter.save(
			Notification.of(writer, serviceTitle, pushBody, NoticeType.COMMUNITY, post.getId(), board.getId()));
		Map<String, String> logIdsByUserId = notificationWriter.saveLogs(targets, notification);

		notificationPushSender.sendToUsers(targets, pushTitle, pushBody, pushData, logIdsByUserId);
	}

	private String resolvePostText(Post post) {
		if (post.getTitle() != null && !post.getTitle().isBlank()) {
			return post.getTitle().trim();
		}
		return NotificationTextUtil.sanitize(post.getContent());
	}
}
