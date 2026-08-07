package net.causw.app.main.domain.notification.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;

@Repository
public interface PostLikeMilestoneAchievementRepository
	extends JpaRepository<PostLikeMilestoneAchievement, String> {

	boolean existsByPostIdAndMilestoneCount(String postId, long milestoneCount);
}
