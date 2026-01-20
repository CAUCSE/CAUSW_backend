package net.causw.app.main.domain.notification.notification.service.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doAnswer;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.notification.notification.repository.NotificationRepository;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeRepository;
import net.causw.app.main.domain.notification.notification.service.v1.BoardNotificationService;
import net.causw.app.main.domain.notification.notification.service.v1.FirebasePushNotificationService;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;
import net.causw.app.main.shared.infra.firebase.FcmUtils;
import net.causw.app.main.util.ObjectFixtures;

import com.google.firebase.messaging.FirebaseMessagingException;

@ExtendWith(MockitoExtension.class)
class BoardNotificationServiceTest {

	@InjectMocks
	private BoardNotificationService boardNotificationService;

	@Mock
	private FirebasePushNotificationService firebasePushNotificationService;

	@Mock
	private UserBlockEntityService userBlockEntityService;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private NotificationLogRepository notificationLogRepository;

	@Mock
	private UserBoardSubscribeRepository userBoardSubscribeRepository;

	@Mock
	private FcmUtils fcmUtils;

	private User mockUser;
	private Board mockBoard;
	private Post mockPost;

	@BeforeEach
	void setUp() {
		mockUser = ObjectFixtures.getUser();
		mockUser.setFcmTokens(new HashSet<>());
		mockUser.getFcmTokens().add("dummy-token");

		mockBoard = mock(Board.class);
		mockPost = mock(Post.class);
	}

	@Test
	@DisplayName("게시판 구독 시 알림 및 로그 저장")
	void sendByBoardIsSubscribedTrue_서비스알림() {
		given(mockPost.getWriter()).willReturn(mockUser);
		given(mockPost.getId()).willReturn("post-id");

		given(userBoardSubscribeRepository.findByBoardAndIsSubscribedTrueExcludingBlockerUsers(mockBoard, Set.of()))
			.willReturn(List.of(UserBoardSubscribe.of(mockUser, mockBoard, true)));

		boardNotificationService.sendByBoardIsSubscribed(mockBoard, mockPost);

		verify(notificationRepository).save(any(Notification.class));
		verify(notificationLogRepository).save(any(NotificationLog.class));
	}

	@Test
	@DisplayName("게시판 구독 안된 경우 알림 로그 저장 안됨")
	void sendByBoardIsSubscribedFalse_서비스알림() {
		given(mockPost.getWriter()).willReturn(mockUser);
		given(mockPost.getId()).willReturn("post-id");

		given(userBoardSubscribeRepository.findByBoardAndIsSubscribedTrueExcludingBlockerUsers(mockBoard, Set.of()))
			.willReturn(List.of());

		boardNotificationService.sendByBoardIsSubscribed(mockBoard, mockPost);

		verify(notificationRepository).save(any(Notification.class));
		verify(notificationLogRepository, never()).save(any(NotificationLog.class));
	}

	@Test
	@DisplayName("정상 토큰일 경우 푸시 알림 전송 성공")
	void sendByBoardIsSubscribedTrue_푸시알림_성공() throws Exception {
		String validToken = "valid-token";
		mockUser.getFcmTokens().add(validToken);

		given(mockPost.getWriter()).willReturn(mockUser);
		given(mockPost.getId()).willReturn("post-id");
		given(mockBoard.getName()).willReturn("게시판 알림");
		given(mockPost.getTitle()).willReturn("게시글 내용");

		given(userBoardSubscribeRepository.findByBoardAndIsSubscribedTrueExcludingBlockerUsers(mockBoard, Set.of()))
			.willReturn(List.of(UserBoardSubscribe.of(mockUser, mockBoard, true)));

		boardNotificationService.sendByBoardIsSubscribed(mockBoard, mockPost);

		verify(firebasePushNotificationService).sendNotification(validToken, "게시판 알림", "새 게시글 : 게시글 내용");
		verify(notificationLogRepository).save(any(NotificationLog.class));
	}

	@Test
	@DisplayName("비정상 토큰일 경우 푸시 알림 실패, 토큰 제거")
	void sendByBoardIsSubscribedTrue_푸시알림_실패() throws Exception {
		String invalidToken = "invalid-token";
		mockUser.getFcmTokens().add(invalidToken);

		given(mockPost.getWriter()).willReturn(mockUser);
		given(mockPost.getId()).willReturn("post-id");

		given(userBoardSubscribeRepository.findByBoardAndIsSubscribedTrueExcludingBlockerUsers(mockBoard, Set.of()))
			.willReturn(List.of(UserBoardSubscribe.of(mockUser, mockBoard, true)));

		FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
		doThrow(mockException)
			.when(firebasePushNotificationService)
			.sendNotification(eq(invalidToken), any(), any());

		doAnswer(invocation -> {
			User user = invocation.getArgument(0);
			String token = invocation.getArgument(1);
			user.removeFcmToken(token);
			return null;
		}).when(fcmUtils).removeFcmToken(any(User.class), anyString());

		boardNotificationService.sendByBoardIsSubscribed(mockBoard, mockPost);

		assertThat(mockUser.getFcmTokens()).doesNotContain(invalidToken);
		verify(firebasePushNotificationService).sendNotification(eq(invalidToken), any(), any());
	}

}
