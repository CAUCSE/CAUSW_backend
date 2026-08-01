package net.causw.app.main.domain.notification.notification.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;
import net.causw.app.main.domain.notification.notification.enums.PostLikeMilestoneAchievementStatus;
import net.causw.app.main.domain.notification.notification.repository.PostLikeMilestoneAchievementRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

@ExtendWith(MockitoExtension.class)
class PostLikeMilestoneAchievementWriterTest {

	@InjectMocks
	private PostLikeMilestoneAchievementWriter writer;

	@Mock
	private PostLikeMilestoneAchievementRepository achievementRepository;

	@Test
	@DisplayName("게시글의 마일스톤 이력이 없으면 PENDING 이력을 저장한다")
	void givenUnconsumedMilestone_whenSavePendingIfAbsent_thenSavePendingAchievement() {
		Post post = postWithId("postId");
		User triggerUser = mock(User.class);
		given(achievementRepository.existsByPostIdAndMilestoneCount("postId", 100L)).willReturn(false);
		given(achievementRepository.saveAndFlush(any(PostLikeMilestoneAchievement.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		Optional<PostLikeMilestoneAchievement> result = writer.savePendingIfAbsent(post, triggerUser, 100L);

		assertThat(result).isPresent();
		assertThat(result.orElseThrow().getPost()).isSameAs(post);
		assertThat(result.orElseThrow().getTriggerUser()).isSameAs(triggerUser);
		assertThat(result.orElseThrow().getMilestoneCount()).isEqualTo(100L);
		assertThat(result.orElseThrow().getStatus()).isEqualTo(PostLikeMilestoneAchievementStatus.PENDING);
	}

	@Test
	@DisplayName("이미 소비한 마일스톤이면 새 이력을 저장하지 않는다")
	void givenConsumedMilestone_whenSavePendingIfAbsent_thenReturnEmpty() {
		Post post = postWithId("postId");
		User triggerUser = mock(User.class);
		given(achievementRepository.existsByPostIdAndMilestoneCount("postId", 100L)).willReturn(true);

		Optional<PostLikeMilestoneAchievement> result = writer.savePendingIfAbsent(post, triggerUser, 100L);

		assertThat(result).isEmpty();
		verify(achievementRepository, never()).saveAndFlush(any());
	}

	private Post postWithId(String postId) {
		Post post = mock(Post.class);
		given(post.getId()).willReturn(postId);
		return post;
	}
}
