package net.causw.app.main.domain.notification.notification.service.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestonePushEvent;
import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

@ExtendWith(MockitoExtension.class)
class PostLikeMilestonePushListenerTest {

	@InjectMocks
	private PostLikeMilestonePushListener listener;

	@Mock
	private UserReader userReader;
	@Mock
	private NotificationPushSender notificationPushSender;

	@Test
	@DisplayName("DB 커밋 후 작성자에게 푸시를 한 번 요청한다")
	void givenCommittedNotification_whenHandle_thenSendPushOnce() {
		User recipient = mock(User.class);
		PushNotificationData pushData = new PushNotificationData(NoticeType.COMMUNITY, "postId", "boardId");
		PostLikeMilestonePushEvent event = new PostLikeMilestonePushEvent(
			"recipientId",
			"push title",
			"push body",
			pushData);
		given(userReader.findUserById("recipientId")).willReturn(recipient);

		listener.handle(event);

		verify(notificationPushSender).sendToUser(recipient, "push title", "push body", pushData);
	}

	@Test
	@DisplayName("푸시 리스너는 DB 트랜잭션 AFTER_COMMIT 단계에서 실행된다")
	void handle_shouldRunAfterCommit() throws NoSuchMethodException {
		Method handleMethod = PostLikeMilestonePushListener.class
			.getMethod("handle", PostLikeMilestonePushEvent.class);

		TransactionalEventListener annotation = handleMethod.getAnnotation(TransactionalEventListener.class);

		assertThat(annotation).isNotNull();
		assertThat(annotation.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
	}
}
