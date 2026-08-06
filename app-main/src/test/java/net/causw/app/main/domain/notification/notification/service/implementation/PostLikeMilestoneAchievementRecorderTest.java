package net.causw.app.main.domain.notification.notification.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneReachedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

@ExtendWith(MockitoExtension.class)
class PostLikeMilestoneAchievementRecorderTest {

	@InjectMocks
	private PostLikeMilestoneAchievementRecorder recorder;

	@Mock
	private PostReader postReader;
	@Mock
	private UserReader userReader;
	@Mock
	private PostLikeMilestoneAchievementWriter achievementWriter;

	@Test
	@DisplayName("신규 마일스톤 이력을 저장하고 이력 ID를 반환한다")
	void givenNewMilestone_whenRecord_thenReturnAchievementId() {
		Post post = mock(Post.class);
		User liker = mock(User.class);
		PostLikeMilestoneAchievement achievement = mock(PostLikeMilestoneAchievement.class);
		given(achievement.getId()).willReturn("achievementId");
		given(postReader.findById("postId")).willReturn(post);
		given(userReader.findUserById("likerId")).willReturn(liker);
		given(achievementWriter.savePendingIfAbsent(post, liker, 100L))
			.willReturn(Optional.of(achievement));

		Optional<String> result = recorder.record(
			new PostLikeMilestoneReachedEvent("postId", "likerId", 100L));

		assertThat(result).contains("achievementId");
	}

	@Test
	@DisplayName("이미 소비한 마일스톤이면 빈 결과를 반환한다")
	void givenConsumedMilestone_whenRecord_thenReturnEmpty() {
		Post post = mock(Post.class);
		User liker = mock(User.class);
		given(postReader.findById("postId")).willReturn(post);
		given(userReader.findUserById("likerId")).willReturn(liker);
		given(achievementWriter.savePendingIfAbsent(post, liker, 100L)).willReturn(Optional.empty());

		Optional<String> result = recorder.record(
			new PostLikeMilestoneReachedEvent("postId", "likerId", 100L));

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("이력 기록은 독립 트랜잭션에서 실행된다")
	void record_shouldUseRequiresNewTransaction() throws NoSuchMethodException {
		Method recordMethod = PostLikeMilestoneAchievementRecorder.class
			.getMethod("record", PostLikeMilestoneReachedEvent.class);

		Transactional annotation = recordMethod.getAnnotation(Transactional.class);

		assertThat(annotation).isNotNull();
		assertThat(annotation.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
	}
}
