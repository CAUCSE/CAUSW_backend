package net.causw.app.main.domain.notification.notification.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;
import net.causw.app.main.domain.notification.notification.repository.PostLikeMilestoneAchievementRepository;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.PostLikeMilestoneAchievementErrorCode;

@ExtendWith(MockitoExtension.class)
class PostLikeMilestoneAchievementReaderTest {

	@InjectMocks
	private PostLikeMilestoneAchievementReader reader;

	@Mock
	private PostLikeMilestoneAchievementRepository achievementRepository;

	@Test
	void givenExistingAchievement_whenFindById_thenReturnAchievement() {
		PostLikeMilestoneAchievement achievement = mock(PostLikeMilestoneAchievement.class);
		given(achievementRepository.findById("achievementId")).willReturn(Optional.of(achievement));

		PostLikeMilestoneAchievement result = reader.findById("achievementId");

		assertThat(result).isSameAs(achievement);
	}

	@Test
	void givenMissingAchievement_whenFindById_thenThrowDomainException() {
		given(achievementRepository.findById("achievementId")).willReturn(Optional.empty());

		assertThatThrownBy(() -> reader.findById("achievementId"))
			.isInstanceOfSatisfying(BaseRunTimeV2Exception.class, exception -> assertThat(exception.getErrorCode())
				.isEqualTo(PostLikeMilestoneAchievementErrorCode.POST_LIKE_MILESTONE_ACHIEVEMENT_NOT_FOUND));
	}
}
