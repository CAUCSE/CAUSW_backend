package net.causw.app.main.domain.notification.notification.entity;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.enums.PostLikeMilestoneAchievementStatus;
import net.causw.app.main.domain.notification.notification.enums.PostLikeMilestoneSuppressionReason;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_post_like_milestone_achievement", uniqueConstraints = {
	@UniqueConstraint(name = "uk_post_like_milestone_achievement_post_milestone", columnNames = {"post_id",
		"milestone_count"})
}, indexes = {
	@Index(name = "idx_post_like_milestone_achievement_trigger_user", columnList = "trigger_user_id")
})
public class PostLikeMilestoneAchievement extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false, updatable = false)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trigger_user_id", updatable = false)
	private User triggerUser;

	@Column(name = "milestone_count", nullable = false, updatable = false)
	private long milestoneCount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 32)
	private PostLikeMilestoneAchievementStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "suppression_reason", length = 32)
	private PostLikeMilestoneSuppressionReason suppressionReason;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_id", unique = true)
	private Notification notification;

	public static PostLikeMilestoneAchievement pending(Post post, User triggerUser, long milestoneCount) {
		return PostLikeMilestoneAchievement.builder()
			.post(post)
			.triggerUser(triggerUser)
			.milestoneCount(milestoneCount)
			.status(PostLikeMilestoneAchievementStatus.PENDING)
			.build();
	}

	public static PostLikeMilestoneAchievement baselined(Post post, long milestoneCount) {
		return PostLikeMilestoneAchievement.builder()
			.post(post)
			.milestoneCount(milestoneCount)
			.status(PostLikeMilestoneAchievementStatus.BASELINED)
			.build();
	}

	public void markNotificationCreated(Notification notification) {
		this.notification = notification;
		this.suppressionReason = null;
		this.status = PostLikeMilestoneAchievementStatus.NOTIFICATION_CREATED;
	}

	public void suppress(PostLikeMilestoneSuppressionReason suppressionReason) {
		this.notification = null;
		this.suppressionReason = suppressionReason;
		this.status = PostLikeMilestoneAchievementStatus.SUPPRESSED;
	}
}
