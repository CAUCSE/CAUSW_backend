package net.causw.app.main.domain.notification.notification.service.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.implementation.ChildCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.CommentChildCommentCreatedEvent;
import net.causw.app.main.domain.notification.notification.event.PostCommentCreatedEvent;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.notification.notification.util.NotificationTextUtil;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentNotificationListener {

	private final PostReader postReader;
	private final CommentReader commentReader;
	private final ChildCommentReader childCommentReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final NotificationSettingReader notificationSettingReader;
	private final BlockReader blockReader;

	/**
	 * [댓글 생성 알림]
	 * 게시글에 댓글이 작성되었을 때, 게시글 작성자에게 알림을 발송하는 핸들러
	 * - 댓글 작성자와 게시글 작성자가 동일한 경우 알림을 보내지 않음
	 * - 게시글 작성자가 댓글 알림을 꺼놓은 경우 알림을 보내지 않음
	 * - 게시글 작성자가 댓글 작성자를 차단한 경우 알림을 보내지 않음
	 * - 알림 방향: commentWriter -> postWriter
	 * @param event 게시글 댓글 작성 이벤트 객체
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleComment(PostCommentCreatedEvent event) {
		// ID로 게시글·댓글 조회
		Post post = postReader.findById(event.postId());
		Comment comment = commentReader.getComment(event.commentId());
		User postWriter = post.getWriter();
		User commentWriter = comment.getWriter();

		// 댓글 작성자와 게시글 작성자가 동일한 경우 알림을 보내지 않음
		if (commentWriter.getId().equals(postWriter.getId())) {
			return;
		}

		// 게시글 작성자가 댓글 알림을 꺼놓은 경우 알림을 보내지 않음
		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(postWriter.getId());
		if (!settingMap.get(UserNotificationSettingKey.COMMUNITY_COMMENT_ON_MY_POST)) {
			return;
		}

		// 게시글 작성자가 댓글 작성자를 차단한 경우 알림을 보내지 않음
		if (blockReader.existsByBlockerAndBlocked(postWriter, commentWriter)) {
			return;
		}

		String displayName = resolveDisplayName(commentWriter, comment.getIsAnonymous());
		String sanitizedContent = NotificationTextUtil.sanitize(comment.getContent());

		// 푸시알림 제목: "내 글에 댓글"
		// 푸시알림 내용: "작성자 님이 댓글을 남겼어요
		// 서비스알림 제목: "작성자 님이 댓글을 남겼어요: " + "댓글 내용 일부"
		String pushTitle = "내 글에 댓글";
		String pushBody = NotificationTextUtil.ellipsis(displayName + "님이 댓글을 남겼어요",
			NotificationTextUtil.PUSH_BODY_MAX_LENGTH);

		String servicePrefix = displayName + "님이 댓글을 남겼어요. ";
		int contentSlot = NotificationTextUtil.SERVICE_TITLE_MAX_LENGTH - servicePrefix.length() - 2;
		String serviceTitle = servicePrefix + "\"" + NotificationTextUtil.ellipsis(sanitizedContent, contentSlot)
			+ "\"";

		Notification notification = notificationWriter.save(
			Notification.of(commentWriter, serviceTitle, pushBody, NoticeType.COMMUNITY, post.getId(),
				post.getBoard().getId()));

		notificationPushSender.sendToUser(postWriter, pushTitle, pushBody);
		notificationWriter.saveLog(postWriter, notification);
	}

	/**
	 * [대댓글 생성 알림]
	 * 댓글에 대댓글이 작성되었을 때, 댓글 작성자에게 알림을 발송하는 핸들러
	 * - 대댓글 작성자와 댓글 작성자가 동일한 경우 알림을 보내지 않음
	 * - 댓글 작성자가 대댓글 알림을 꺼놓은 경우 알림을 보내지 않음
	 * - 댓글 작성자가 대댓글 작성자를 차단한 경우 알림을 보내지 않음
	 * - 대댓글 작성자가 댓글 작성자를 차단한 경우 알림을 보내지 않음
	 * - 알림 방향: childCommentWriter -> commentWriter
	 * @param event
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleChildComment(CommentChildCommentCreatedEvent event) {
		// ID로 댓글·대댓글 조회
		Comment comment = commentReader.getComment(event.commentId());
		ChildComment childComment = childCommentReader.findById(event.childCommentId());
		Post post = comment.getPost();
		User commentWriter = comment.getWriter();
		User childCommentWriter = childComment.getWriter();

		// 대댓글 작성자와 댓글 작성자가 동일한 경우 알림을 보내지 않음
		if (childCommentWriter.getId().equals(commentWriter.getId())) {
			return;
		}

		// 댓글 작성자가 대댓글 알림을 꺼놓은 경우 알림을 보내지 않음
		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(commentWriter.getId());
		if (!settingMap.get(UserNotificationSettingKey.COMMUNITY_REPLY_ON_MY_COMMENT)) {
			return;
		}

		// 댓글 작성자가 대댓글 작성자를 차단한 경우 알림을 보내지 않음
		if (blockReader.existsByBlockerAndBlocked(commentWriter, childCommentWriter)) {
			return;
		}

		String displayName = resolveDisplayName(childCommentWriter, childComment.getIsAnonymous());
		String sanitizedContent = NotificationTextUtil.sanitize(childComment.getContent());

		// 푸시알림 제목: "내 댓글에 답글"
		// 푸시알림 내용: "작성자 님이 답글을 남겼어요"
		// 서비스알림 제목: "작성자 님이 답글을 남겼어요: " + "대댓글 내용 일부"
		String pushTitle = "내 댓글에 답글";
		String pushBody = NotificationTextUtil.ellipsis(displayName + "님이 답글을 남겼어요",
			NotificationTextUtil.PUSH_BODY_MAX_LENGTH);

		String servicePrefix = displayName + "님이 답글을 남겼어요: ";
		int contentSlot = NotificationTextUtil.SERVICE_TITLE_MAX_LENGTH - servicePrefix.length() - 2;
		String serviceTitle = servicePrefix + "\"" + NotificationTextUtil.ellipsis(sanitizedContent, contentSlot)
			+ "\"";

		Notification notification = notificationWriter.save(
			Notification.of(childCommentWriter, serviceTitle, pushBody, NoticeType.COMMUNITY, post.getId(),
				post.getBoard().getId()));

		notificationPushSender.sendToUser(commentWriter, pushTitle, pushBody);
		notificationWriter.saveLog(commentWriter, notification);
	}

	private static String resolveDisplayName(User user, boolean isAnonymous) {
		if (isAnonymous) {
			return StaticValue.ANONYMOUS_USER_NICKNAME;
		}

		return user.getNickname() != null ? user.getNickname() : user.getName();
	}

}
