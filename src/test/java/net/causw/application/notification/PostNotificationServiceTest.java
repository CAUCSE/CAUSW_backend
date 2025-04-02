package net.causw.application.notification;

import com.google.firebase.messaging.FirebaseMessagingException;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.notification.UserPostSubscribe;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.repository.notification.UserPostSubscribeRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.user.UserCreateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostNotificationServiceTest {

    @InjectMocks
    private PostNotificationService postNotificationService;

    @Mock
    private FirebasePushNotificationService firebasePushNotificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private UserPostSubscribeRepository userPostSubscribeRepository;

    private User mockUser;
    private Post mockPost;
    private Comment mockComment;

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

        mockPost = mock(Post.class);
        mockComment = mock(Comment.class);
    }

    @Test
    @DisplayName("게시글 구독 시 알림 및 로그 저장")
    void sendByPostIsSubscribed_성공() {
        given(mockPost.getId()).willReturn("post-id");
        lenient().when(mockComment.getPost()).thenReturn(mockPost);

        given(userPostSubscribeRepository.findByPostAndIsSubscribedTrue(mockPost))
                .willReturn(List.of(UserPostSubscribe.of(mockUser, mockPost, true)));

        postNotificationService.sendByPostIsSubscribed(mockPost, mockComment);

        verify(notificationRepository).save(any(Notification.class));
        verify(notificationLogRepository).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("게시글 구독 안된 경우 알림 저장, 로그 저장 안됨")
    void sendByPostIsSubscribed_구독없음() {
        given(mockPost.getId()).willReturn("post-id");
        lenient().when(mockComment.getPost()).thenReturn(mockPost);

        given(userPostSubscribeRepository.findByPostAndIsSubscribedTrue(mockPost))
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
        lenient().when(mockComment.getPost()).thenReturn(mockPost);
        given(mockPost.getTitle()).willReturn("게시글 제목");
        given(mockComment.getContent()).willReturn("댓글 내용");


        given(userPostSubscribeRepository.findByPostAndIsSubscribedTrue(mockPost))
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
        given(mockComment.getPost()).willReturn(mockPost);

        given(userPostSubscribeRepository.findByPostAndIsSubscribedTrue(mockPost))
                .willReturn(List.of(UserPostSubscribe.of(mockUser, mockPost, true)));

        FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
        doThrow(mockException).when(firebasePushNotificationService)
                .sendNotification(eq(invalidToken), any(), any());

        postNotificationService.sendByPostIsSubscribed(mockPost, mockComment);

        assertThat(mockUser.getFcmTokens()).doesNotContain(invalidToken);
        verify(firebasePushNotificationService).sendNotification(eq(invalidToken), any(), any());
    }
}
