package net.causw.app.main.domain.notification.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.enums.PostLikeMilestoneAchievementStatus;
import net.causw.app.main.domain.notification.notification.enums.PostLikeMilestoneSuppressionReason;
import net.causw.app.main.domain.user.account.entity.user.User;

class PostLikeMilestoneAchievementTest {

	@Nested
	@DisplayName("생성")
	class CreationTest {

		@Test
		@DisplayName("신규 마일스톤은 트리거 사용자와 함께 대기 상태로 생성한다")
		void givenPostAndTriggerUser_whenCreatePending_thenSetPendingStatus() {
			Post post = mock(Post.class);
			User triggerUser = mock(User.class);

			PostLikeMilestoneAchievement achievement = PostLikeMilestoneAchievement.pending(post, triggerUser, 100L);

			assertThat(achievement.getPost()).isSameAs(post);
			assertThat(achievement.getTriggerUser()).isSameAs(triggerUser);
			assertThat(achievement.getMilestoneCount()).isEqualTo(100L);
			assertThat(achievement.getStatus()).isEqualTo(PostLikeMilestoneAchievementStatus.PENDING);
			assertThat(achievement.getSuppressionReason()).isNull();
			assertThat(achievement.getNotification()).isNull();
		}

		@Test
		@DisplayName("기존 게시글의 도달 마일스톤은 트리거 사용자 없이 기준선 상태로 생성한다")
		void givenExistingPost_whenCreateBaselined_thenSetBaselinedStatus() {
			Post post = mock(Post.class);

			PostLikeMilestoneAchievement achievement = PostLikeMilestoneAchievement.baselined(post, 500L);

			assertThat(achievement.getPost()).isSameAs(post);
			assertThat(achievement.getTriggerUser()).isNull();
			assertThat(achievement.getMilestoneCount()).isEqualTo(500L);
			assertThat(achievement.getStatus()).isEqualTo(PostLikeMilestoneAchievementStatus.BASELINED);
			assertThat(achievement.getSuppressionReason()).isNull();
			assertThat(achievement.getNotification()).isNull();
		}
	}

	@Nested
	@DisplayName("처리 결과 기록")
	class ResultRecordingTest {

		@Test
		@DisplayName("인앱 알림 생성 결과를 알림과 함께 기록한다")
		void givenPendingAchievement_whenMarkNotificationCreated_thenRecordNotification() {
			PostLikeMilestoneAchievement achievement = pendingAchievement();
			Notification notification = mock(Notification.class);

			achievement.markNotificationCreated(notification);

			assertThat(achievement.getStatus())
				.isEqualTo(PostLikeMilestoneAchievementStatus.NOTIFICATION_CREATED);
			assertThat(achievement.getNotification()).isSameAs(notification);
			assertThat(achievement.getSuppressionReason()).isNull();
		}

		@Test
		@DisplayName("알림 억제 결과를 사유와 함께 기록한다")
		void givenPendingAchievement_whenSuppress_thenRecordReason() {
			PostLikeMilestoneAchievement achievement = pendingAchievement();

			achievement.suppress(PostLikeMilestoneSuppressionReason.SETTING_DISABLED);

			assertThat(achievement.getStatus()).isEqualTo(PostLikeMilestoneAchievementStatus.SUPPRESSED);
			assertThat(achievement.getSuppressionReason())
				.isEqualTo(PostLikeMilestoneSuppressionReason.SETTING_DISABLED);
			assertThat(achievement.getNotification()).isNull();
		}
	}

	private PostLikeMilestoneAchievement pendingAchievement() {
		return PostLikeMilestoneAchievement.pending(mock(Post.class), mock(User.class), 100L);
	}
}
