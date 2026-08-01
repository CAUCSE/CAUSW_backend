package net.causw.app.main.domain.notification.notification.service.implementation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.common.service.CommunityPermissionPolicy;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.enums.PostLikeMilestoneAchievementStatus;
import net.causw.app.main.domain.notification.notification.enums.PostLikeMilestoneSuppressionReason;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestonePushEvent;
import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostLikeMilestoneNotificationProcessor {

	private final PostLikeMilestoneAchievementReader achievementReader;
	private final PostLikeMilestoneAchievementWriter achievementWriter;
	private final NotificationWriter notificationWriter;
	private final NotificationSettingReader notificationSettingReader;
	private final BlockReader blockReader;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void process(String achievementId) {
		PostLikeMilestoneAchievement achievement = achievementReader.findById(achievementId);
		if (achievement.getStatus() != PostLikeMilestoneAchievementStatus.PENDING) {
			return;
		}

		Post post = achievement.getPost();
		User liker = achievement.getTriggerUser();
		User postWriter = post == null ? null : post.getWriter();

		if (liker == null || isTargetUnavailable(post, postWriter)) {
			achievementWriter.suppress(achievement, PostLikeMilestoneSuppressionReason.TARGET_UNAVAILABLE);
			return;
		}

		if (liker.getId().equals(postWriter.getId())) {
			achievementWriter.suppress(achievement, PostLikeMilestoneSuppressionReason.SELF_LIKE);
			return;
		}

		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(postWriter.getId());
		if (!settingMap.get(UserNotificationSettingKey.COMMUNITY_LIKE_ON_MY_POST)) {
			achievementWriter.suppress(achievement, PostLikeMilestoneSuppressionReason.SETTING_DISABLED);
			return;
		}

		if (blockReader.existsByBlockerAndBlocked(postWriter, liker)) {
			achievementWriter.suppress(achievement, PostLikeMilestoneSuppressionReason.BLOCKED);
			return;
		}

		long likeCount = achievement.getMilestoneCount();
		String serviceTitle = String.format("게시물이 좋아요 %d개를 달성했습니다!", likeCount);
		String serviceBody = String.format("내 게시글에 좋아요가 %d개 달렸어요.", likeCount);
		String pushTitle = String.format("게시물 좋아요 %d개 달성", likeCount);
		PushNotificationData pushData = new PushNotificationData(NoticeType.COMMUNITY, post.getId(),
			post.getBoard().getId());

		Notification notification = notificationWriter.save(
			Notification.of(postWriter, serviceTitle, serviceBody, NoticeType.COMMUNITY, post.getId(),
				post.getBoard().getId()));
		notificationWriter.saveLog(postWriter, notification);
		achievementWriter.markNotificationCreated(achievement, notification);

		eventPublisher.publishEvent(new PostLikeMilestonePushEvent(
			postWriter.getId(),
			pushTitle,
			serviceBody,
			pushData));
	}

	private boolean isTargetUnavailable(Post post, User postWriter) {
		return !CommunityPermissionPolicy.isAlive(post)
			|| postWriter == null
			|| postWriter.isInactive()
			|| postWriter.isDropped();
	}
}
