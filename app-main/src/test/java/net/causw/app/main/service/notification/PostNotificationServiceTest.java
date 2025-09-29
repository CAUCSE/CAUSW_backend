package net.causw.app.main.service.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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

import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.notification.Notification;
import net.causw.app.main.domain.model.entity.notification.NotificationLog;
import net.causw.app.main.domain.model.entity.notification.UserPostSubscribe;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.user.UserCreateRequestDto;
import net.causw.app.main.infrastructure.firebase.FcmUtils;
import net.causw.app.main.repository.notification.NotificationLogRepository;
import net.causw.app.main.repository.notification.NotificationRepository;
import net.causw.app.main.repository.notification.UserPostSubscribeRepository;
import net.causw.app.main.service.userBlock.UserBlockEntityService;
import net.causw.app.main.util.ObjectFixtures;

import com.google.firebase.messaging.FirebaseMessagingException;

@ExtendWith(MockitoExtension.class)
class PostNotificationServiceTest {

	@InjectMocks
	private PostNotificationService postNotificationService;

	@Mock
	private UserBlockEntityService userBlockEntityService;

	@Mock
	private FirebasePushNotificationService firebasePushNotificationService;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private NotificationLogRepository notificationLogRepository;

	@Mock
	private UserPostSubscribeRepository userPostSubscribeRepository;

	@Mock
	private FcmUtils fcmUtils;

	private User mockUser;
	private Post mockPost;
	private Board mockBoard;
	private Comment mockComment;

	@BeforeEach
	void setUp() {
		mockUser = ObjectFixtures.getUser();
		mockUser.setFcmTokens(new HashSet<>());
		mockUser.getFcmTokens().add("dummy-token");

		mockBoard = mock(Board.class);
		mockPost = mock(Post.class);
		mockComment = mock(Comment.class);
	}

	@Test
	@DisplayName("게시글 구독 시 알림 및 로그 저장")
	void sendByPostIsSubscribed_성공() {
		given(mockPost.getId()).willReturn("post-id");
		given(mockPost.getBoard()).willReturn(mockBoard);
		given(mockBoard.getId()).willReturn("board-id");
		lenient().when(mockComment.getPost()).thenReturn(mockPost);

		given(userPostSubscribeRepository.findByPostAndIsSubscribedTrueExcludingBlockers(mockPost, Set.of()))
			.willReturn(List.of(UserPostSubscribe.of(mockUser, mockPost, true)));

		postNotificationService.sendByPostIsSubscribed(mockPost, mockComment);

		verify(notificationRepository).save(any(Notification.class));
		verify(notificationLogRepository).save(any(NotificationLog.class));
	}

	@Test
	@DisplayName("게시글 구독 안된 경우 알림 저장, 로그 저장 안됨")
	void sendByPostIsSubscribed_구독없음() {
		given(mockPost.getId()).willReturn("post-id");
		given(mockPost.getBoard()).willReturn(mockBoard);
		given(mockBoard.getId()).willReturn("board-id");
		lenient().when(mockComment.getPost()).thenReturn(mockPost);

		given(userPostSubscribeRepository.findByPostAndIsSubscribedTrueExcludingBlockers(mockPost, Set.of()))
			.willReturn(List.of());

		postNotificationService.sendByPostIsSubscribed(mockPost, mockComment);

		verify(notificationRepository).save(any(Notification.class));
		verify(notificationLogRepository, never()).save(any(NotificationLog.class));
	}

	@Test
	@DisplayName("정상 토큰일 경우 푸시 알림 전송 성공")
	void sendByPostIsSubscribed_푸시성공() throws Exception {
		mockUser.getFcmTokens().add("valid-token");

		given(mockPost.getId()).willReturn("post-id");
		given(mockPost.getBoard()).willReturn(mockBoard);
		given(mockBoard.getId()).willReturn("board-id");
		lenient().when(mockComment.getPost()).thenReturn(mockPost);
		given(mockPost.getTitle()).willReturn("게시글 제목");
		given(mockComment.getContent()).willReturn("댓글 내용");

		given(userPostSubscribeRepository.findByPostAndIsSubscribedTrueExcludingBlockers(mockPost, Set.of()))
			.willReturn(List.of(UserPostSubscribe.of(mockUser, mockPost, true)));

		postNotificationService.sendByPostIsSubscribed(mockPost, mockComment);

		verify(firebasePushNotificationService).sendNotification("valid-token", "게시글 제목", "새 댓글 : 댓글 내용");
		verify(notificationLogRepository).save(any(NotificationLog.class));
	}

	@Test
	@DisplayName("비정상 토큰일 경우 푸시 알림 실패, 토큰 제거")
	void sendByPostIsSubscribed_푸시실패() throws Exception {
		String invalidToken = "invalid-token";
		mockUser.getFcmTokens().add(invalidToken);

		given(mockPost.getId()).willReturn("post-id");
		given(mockPost.getBoard()).willReturn(mockBoard);
		given(mockBoard.getId()).willReturn("board-id");
		given(mockComment.getPost()).willReturn(mockPost);

		given(userPostSubscribeRepository.findByPostAndIsSubscribedTrueExcludingBlockers(mockPost, Set.of()))
			.willReturn(List.of(UserPostSubscribe.of(mockUser, mockPost, true)));

		FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
		doThrow(mockException).when(firebasePushNotificationService)
			.sendNotification(eq(invalidToken), any(), any());

		doAnswer(invocation -> {
			User user = invocation.getArgument(0);
			String token = invocation.getArgument(1);
			user.removeFcmToken(token);
			return null;
		}).when(fcmUtils).removeFcmToken(any(User.class), anyString());

		postNotificationService.sendByPostIsSubscribed(mockPost, mockComment);

		assertThat(mockUser.getFcmTokens()).doesNotContain(invalidToken);
		verify(firebasePushNotificationService).sendNotification(eq(invalidToken), any(), any());
	}
}
