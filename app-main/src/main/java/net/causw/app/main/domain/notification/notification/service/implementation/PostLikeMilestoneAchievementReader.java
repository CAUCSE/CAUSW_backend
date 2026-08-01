package net.causw.app.main.domain.notification.notification.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;
import net.causw.app.main.domain.notification.notification.repository.PostLikeMilestoneAchievementRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeMilestoneAchievementReader {

	private final PostLikeMilestoneAchievementRepository achievementRepository;

	public PostLikeMilestoneAchievement findById(String achievementId) {
		return achievementRepository.findById(achievementId)
			.orElseThrow(() -> new IllegalStateException(
				"게시글 좋아요 마일스톤 이력을 찾을 수 없습니다: " + achievementId));
	}
}
