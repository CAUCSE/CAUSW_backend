package net.causw.app.main.domain.notification.notification.service.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneReachedEvent;
import net.causw.app.main.domain.notification.notification.service.implementation.PostLikeMilestoneAchievementRecorder;
import net.causw.app.main.domain.notification.notification.service.implementation.PostLikeMilestoneNotificationProcessor;

@ExtendWith(MockitoExtension.class)
class PostLikeMilestoneReachedListenerTest {

	@InjectMocks
	private PostLikeMilestoneReachedListener listener;

	@Mock
	private PostLikeMilestoneAchievementRecorder achievementRecorder;
	@Mock
	private PostLikeMilestoneNotificationProcessor notificationProcessor;

	@Test
	@DisplayName("신규 이력의 트랜잭션이 완료되면 알림 처리를 순차 호출한다")
	void givenNewAchievement_whenHandle_thenProcessNotification() {
		PostLikeMilestoneReachedEvent event = new PostLikeMilestoneReachedEvent("postId", "likerId", 100L);
		given(achievementRecorder.record(event)).willReturn(Optional.of("achievementId"));

		listener.handle(event);

		verify(notificationProcessor).process("achievementId");
	}

	@Test
	@DisplayName("이미 소비된 마일스톤이면 알림 처리를 호출하지 않는다")
	void givenConsumedAchievement_whenHandle_thenSkipNotificationProcessing() {
		PostLikeMilestoneReachedEvent event = new PostLikeMilestoneReachedEvent("postId", "likerId", 100L);
		given(achievementRecorder.record(event)).willReturn(Optional.empty());

		listener.handle(event);

		verifyNoInteractions(notificationProcessor);
	}

	@Test
	@DisplayName("마일스톤 도달 리스너는 좋아요 트랜잭션 AFTER_COMMIT 단계에서 실행된다")
	void handle_shouldRunAfterCommit() throws NoSuchMethodException {
		Method handleMethod = PostLikeMilestoneReachedListener.class
			.getMethod("handle", PostLikeMilestoneReachedEvent.class);

		TransactionalEventListener annotation = handleMethod.getAnnotation(TransactionalEventListener.class);

		assertThat(annotation).isNotNull();
		assertThat(annotation.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
	}
}
