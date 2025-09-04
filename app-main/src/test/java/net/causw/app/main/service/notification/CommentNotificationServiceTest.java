package net.causw.app.main.service.notification;

import com.google.firebase.messaging.FirebaseMessagingException;

import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.notification.Notification;
import net.causw.app.main.domain.model.entity.notification.NotificationLog;
import net.causw.app.main.domain.model.entity.notification.UserCommentSubscribe;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.infrastructure.firebase.FcmUtils;
import net.causw.app.main.repository.notification.NotificationLogRepository;
import net.causw.app.main.repository.notification.NotificationRepository;
import net.causw.app.main.repository.notification.UserCommentSubscribeRepository;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.user.UserCreateRequestDto;
import net.causw.app.main.service.userBlock.UserBlockEntityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentNotificationServiceTest {

	@InjectMocks
	private CommentNotificationService commentNotificationService;

	@Mock
	private FirebasePushNotificationService firebasePushNotificationService;

	@Mock
	private UserBlockEntityService userBlockEntityService;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private NotificationLogRepository notificationLogRepository;

	@Mock
	private UserCommentSubscribeRepository userCommentSubscribeRepository;

	@Mock
	private FcmUtils fcmUtils;

	private User mockUser;
	private Comment mockComment;
	private ChildComment mockChildComment;
	private Post mockPost;
	private Board mockBoard;

	@BeforeEach
	void setUp() {
		UserCreateRequestDto userCreateRequestDto = UserCreateRequestDto.builder()
			.email("test@cau.ac.kr")
			.name("테스트 유저")
			.password("Password123!")
			.studentId("20235555")
			.admissionYear(2023)
			.nickname("tester")
			.major("소프트웨어학부")
			.phoneNumber("010-1234-5678")
			.build();

		mockUser = User.from(userCreateRequestDto, "encodedPassword");
		mockUser.setFcmTokens(new HashSet<>());
		mockUser.getFcmTokens().add("dummy-token");

		mockBoard = mock(Board.class);
		mockPost = mock(Post.class);
		mockComment = mock(Comment.class);
		mockChildComment = mock(ChildComment.class);

		lenient().when(mockComment.getPost()).thenReturn(mockPost);
	}

	@Test
	@DisplayName("댓글 구독 시 알림 저장, 로그 저장")
	void sendByCommentIsSubscribed_성공() {
		given(mockPost.getId()).willReturn("post-id");
		given(mockChildComment.getWriter()).willReturn(mockUser);
		given(mockPost.getBoard()).willReturn(mockBoard);
		given(mockBoard.getId()).willReturn("board-id");

		Set<String> blockerUserIds = Set.of();
		given(userCommentSubscribeRepository.findByCommentAndIsSubscribedTrueExcludingBlockerUsers(mockComment,
			blockerUserIds))
			.willReturn(List.of(UserCommentSubscribe.of(mockUser, mockComment, true)));

		commentNotificationService.sendByCommentIsSubscribed(mockComment, mockChildComment);

		verify(notificationRepository).save(any(Notification.class));
		verify(notificationLogRepository).save(any(NotificationLog.class));
	}

	@Test
	@DisplayName("댓글 구독 안된 경우 알림 저장, 로그 저장 안됨")
	void sendByCommentIsSubscribed_구독없음() {
		given(mockPost.getId()).willReturn("post-id");
		given(mockPost.getBoard()).willReturn(mockBoard);
		given(mockBoard.getId()).willReturn("board-id");
		Set<String> blockerUserIds = Set.of();
		given(userCommentSubscribeRepository.findByCommentAndIsSubscribedTrueExcludingBlockerUsers(mockComment,
			blockerUserIds))
			.willReturn(List.of());

		commentNotificationService.sendByCommentIsSubscribed(mockComment, mockChildComment);

		verify(notificationRepository).save(any(Notification.class));
		verify(notificationLogRepository, never()).save(any(NotificationLog.class));
	}

	@Test
	@DisplayName("정상 토큰일 경우 푸시 알림 전송 성공")
	void sendByCommentIsSubscribed_푸시성공() throws Exception {
		String validToken = "valid-token";
		mockUser.getFcmTokens().add(validToken);

		given(mockPost.getId()).willReturn("post-id");
		given(mockPost.getBoard()).willReturn(mockBoard);
		given(mockBoard.getId()).willReturn("board-id");
		given(mockChildComment.getWriter()).willReturn(mockUser);
		given(mockComment.getContent()).willReturn("부모 댓글");
		given(mockChildComment.getContent()).willReturn("대댓글 내용");
		Set<String> blockerUserIds = Set.of();
		given(userCommentSubscribeRepository.findByCommentAndIsSubscribedTrueExcludingBlockerUsers(mockComment,
			blockerUserIds))
			.willReturn(List.of(UserCommentSubscribe.of(mockUser, mockComment, true)));

		commentNotificationService.sendByCommentIsSubscribed(mockComment, mockChildComment);

		verify(firebasePushNotificationService).sendNotification(validToken, "부모 댓글", "새 대댓글 : 대댓글 내용");
		verify(notificationLogRepository).save(any(NotificationLog.class));
	}

	@Test
	@DisplayName("비정상 토큰일 경우 푸시 알림 실패, 토큰 제거")
	void sendByCommentIsSubscribed_푸시실패() throws Exception {
		String invalidToken = "invalid-token";
		mockUser.getFcmTokens().add(invalidToken);

		given(mockPost.getId()).willReturn("post-id");
		given(mockPost.getBoard()).willReturn(mockBoard);
		given(mockBoard.getId()).willReturn("board-id");
		given(mockChildComment.getWriter()).willReturn(mockUser);
		Set<String> blockerUserIds = Set.of();
		given(userCommentSubscribeRepository.findByCommentAndIsSubscribedTrueExcludingBlockerUsers(mockComment,
			blockerUserIds))
			.willReturn(List.of(UserCommentSubscribe.of(mockUser, mockComment, true)));

		FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
		doThrow(mockException).when(firebasePushNotificationService)
			.sendNotification(eq(invalidToken), any(), any());

		doAnswer(invocation -> {
			User user = invocation.getArgument(0);
			String token = invocation.getArgument(1);
			user.removeFcmToken(token);
			return null;
		}).when(fcmUtils).removeFcmToken(any(User.class), anyString());

		commentNotificationService.sendByCommentIsSubscribed(mockComment, mockChildComment);

		assertThat(mockUser.getFcmTokens()).doesNotContain(invalidToken);
		verify(firebasePushNotificationService).sendNotification(eq(invalidToken), any(), any());
	}
}
