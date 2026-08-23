package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;
import net.causw.app.main.domain.notification.notification.enums.PostLikeMilestoneSuppressionReason;
import net.causw.app.main.domain.notification.notification.repository.PostLikeMilestoneAchievementRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class PostLikeMilestoneAchievementWriter {

	private final PostLikeMilestoneAchievementRepository achievementRepository;

	public Optional<PostLikeMilestoneAchievement> savePendingIfAbsent(
		Post post,
		User triggerUser,
		long milestoneCount) {
		if (achievementRepository.existsByPostIdAndMilestoneCount(post.getId(), milestoneCount)) {
			return Optional.empty();
		}

		PostLikeMilestoneAchievement achievement = PostLikeMilestoneAchievement.pending(
			post,
			triggerUser,
			milestoneCount);
		return Optional.of(achievementRepository.saveAndFlush(achievement));
	}

	public PostLikeMilestoneAchievement suppress(
		PostLikeMilestoneAchievement achievement,
		PostLikeMilestoneSuppressionReason suppressionReason) {
		achievement.suppress(suppressionReason);
		return achievementRepository.save(achievement);
	}

	public PostLikeMilestoneAchievement markNotificationCreated(
		PostLikeMilestoneAchievement achievement,
		Notification notification) {
		achievement.markNotificationCreated(notification);
		return achievementRepository.save(achievement);
	}
}
