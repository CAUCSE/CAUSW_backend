package net.causw.app.main.domain.notification.notification.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;
import net.causw.app.main.domain.notification.notification.repository.PostLikeMilestoneAchievementRepository;
import net.causw.app.main.shared.exception.errorcode.PostLikeMilestoneAchievementErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeMilestoneAchievementReader {

	private final PostLikeMilestoneAchievementRepository achievementRepository;

	public PostLikeMilestoneAchievement findById(String achievementId) {
		return achievementRepository.findById(achievementId)
			.orElseThrow(
				PostLikeMilestoneAchievementErrorCode.POST_LIKE_MILESTONE_ACHIEVEMENT_NOT_FOUND::toBaseException);
	}
}
