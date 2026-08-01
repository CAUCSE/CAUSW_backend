package net.causw.app.main.domain.notification.notification.service.listener;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneNotificationEvent;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneReachedEvent;
import net.causw.app.main.domain.notification.notification.service.implementation.PostLikeMilestoneAchievementWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

@ExtendWith(MockitoExtension.class)
class PostLikeMilestoneAchievementListenerTest {

	@InjectMocks
	private PostLikeMilestoneAchievementListener listener;

	@Mock
	private PostReader postReader;
	@Mock
	private UserReader userReader;
	@Mock
	private PostLikeMilestoneAchievementWriter achievementWriter;
	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Test
	@DisplayName("신규 마일스톤 이력이 저장되면 이력 ID로 알림 이벤트를 발행한다")
	void givenNewMilestone_whenHandle_thenPublishNotificationEvent() {
		Post post = mock(Post.class);
		User liker = mock(User.class);
		PostLikeMilestoneAchievement achievement = mock(PostLikeMilestoneAchievement.class);
		given(achievement.getId()).willReturn("achievementId");
		given(postReader.findById("postId")).willReturn(post);
		given(userReader.findUserById("likerId")).willReturn(liker);
		given(achievementWriter.savePendingIfAbsent(post, liker, 100L))
			.willReturn(Optional.of(achievement));

		listener.handle(new PostLikeMilestoneReachedEvent("postId", "likerId", 100L));

		verify(eventPublisher).publishEvent(new PostLikeMilestoneNotificationEvent("achievementId"));
	}

	@Test
	@DisplayName("이미 소비한 마일스톤이면 알림 이벤트를 발행하지 않는다")
	void givenConsumedMilestone_whenHandle_thenDoNotPublishNotificationEvent() {
		Post post = mock(Post.class);
		User liker = mock(User.class);
		given(postReader.findById("postId")).willReturn(post);
		given(userReader.findUserById("likerId")).willReturn(liker);
		given(achievementWriter.savePendingIfAbsent(post, liker, 100L)).willReturn(Optional.empty());

		listener.handle(new PostLikeMilestoneReachedEvent("postId", "likerId", 100L));

		verifyNoInteractions(eventPublisher);
	}
}
