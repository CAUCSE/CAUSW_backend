package net.causw.app.main.domain.notification.notification.service.policy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LikePostNotificationPolicyTest {

	@ParameterizedTest(name = "좋아요 {0}개")
	@ValueSource(longs = {1, 2, 3, 4, 5, 10, 50, 100, 500, 1000, 2000, 10000})
	@DisplayName("정책에 정의된 좋아요 수는 마일스톤이다")
	void givenMilestoneCount_whenCheck_thenReturnTrue(long likeCount) {
		assertThat(LikePostNotificationPolicy.isMilestone(likeCount)).isTrue();
	}

	@ParameterizedTest(name = "좋아요 {0}개")
	@ValueSource(longs = {-1, 0, 6, 9, 11, 49, 51, 99, 101, 499, 501, 999, 1001})
	@DisplayName("정책에 정의되지 않은 좋아요 수는 마일스톤이 아니다")
	void givenNonMilestoneCount_whenCheck_thenReturnFalse(long likeCount) {
		assertThat(LikePostNotificationPolicy.isMilestone(likeCount)).isFalse();
	}
}
