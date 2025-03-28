package net.causw.application.notification;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.notification.NotificationResponseDto;
import net.causw.application.dto.user.UserCreateRequestDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.model.enums.notification.NoticeType;
import net.causw.domain.model.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;


@ExtendWith(MockitoExtension.class)
public class NotificationLogServiceTest {
    @InjectMocks
    private NotificationLogService notificationLogService;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    private User mockUser;

    private List<NotificationLog> mockNotificationLogs;

    @BeforeEach
    void setUp() {
        UserCreateRequestDto userCreateRequestDto = UserCreateRequestDto.builder()
                .email("tester@example.com")
                .name("테스트 유저")
                .password("Password123!")
                .studentId("20231234")
                .admissionYear(2023)
                .nickname("testerNick")
                .major("컴퓨터공학과")
                .phoneNumber("010-1234-5678")
                .build();
        mockUser = User.from(userCreateRequestDto, "encodedPassword");
    }

    @Nested
    @DisplayName("getCeremonyNotification() 테스트")
    class GetCeremonyNotificationTest {

        @BeforeEach
        void setUpCeremonyNotification() {
            Notification ceremonyNotification = Notification.of(mockUser, "경조사 제목", "내용", NoticeType.CEREMONY, "경조사-id");
            NotificationLog ceremonyLog = NotificationLog.of(mockUser, ceremonyNotification);
            mockNotificationLogs = List.of(ceremonyLog);
        }

        @Test
        @DisplayName("경조사 알림 조회 성공")
        void getCeremonyNotificationSuccess() {
            List<NoticeType> types = Arrays.asList(NoticeType.CEREMONY);
            given(notificationLogRepository.findByUserAndNotificationTypes(mockUser, types))
                    .willReturn(mockNotificationLogs);

            List<NotificationResponseDto> result = notificationLogService.getCeremonyNotification(mockUser);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("경조사 제목");

            verify(notificationLogRepository).findByUserAndNotificationTypes(mockUser, types);
        }

        @Test
        @DisplayName("경조사 알림이 없는 경우 - 빈 리스트 반환")
        void getCeremonyNotificationEmpty() {
            List<NoticeType> types = List.of(NoticeType.CEREMONY);

            given(notificationLogRepository.findByUserAndNotificationTypes(mockUser, types))
                    .willReturn(List.of());

            List<NotificationResponseDto> result = notificationLogService.getCeremonyNotification(mockUser);

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(notificationLogRepository).findByUserAndNotificationTypes(mockUser, types);
        }
    }

    @Nested
    @DisplayName("getGeneralNotification() 테스트")
    class GetGeneralNotificationTest {

        @BeforeEach
        void setUpGeneralNotification() {
            Notification boardNotification = Notification.of(mockUser, "게시판 제목", "게시글 내용", NoticeType.BOARD, "게시글-id");
            Notification postNotification = Notification.of(mockUser, "게시글 제목", "댓글 내용", NoticeType.POST, "게시글-id");
            Notification commentNotification = Notification.of(mockUser, "댓글 내용", "대댓글 내용", NoticeType.COMMENT, "게시글-id");

            NotificationLog boardLog = NotificationLog.of(mockUser, boardNotification);
            NotificationLog postLog = NotificationLog.of(mockUser, postNotification);
            NotificationLog commentLog = NotificationLog.of(mockUser, commentNotification);

            mockNotificationLogs = List.of(boardLog, postLog, commentLog);
        }

        @Test
        @DisplayName("일반 알림 조회 성공")
        void getGeneralNotificationSuccess() {
            List<NoticeType> types = Arrays.asList(NoticeType.BOARD, NoticeType.POST, NoticeType.COMMENT);

            given(notificationLogRepository.findByUserAndNotificationTypes(mockUser, types))
                    .willReturn(mockNotificationLogs);

            List<NotificationResponseDto> result = notificationLogService.getGeneralNotification(mockUser);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            assertThat(result).extracting("noticeType")
                    .containsExactlyInAnyOrder(NoticeType.BOARD, NoticeType.POST, NoticeType.COMMENT);

            verify(notificationLogRepository).findByUserAndNotificationTypes(mockUser, types);
        }

        @Test
        @DisplayName("일반 알림이 없는 경우 - 빈 리스트 반환")
        void getGeneralNotificationEmpty() {
            List<NoticeType> types = List.of(NoticeType.BOARD, NoticeType.POST, NoticeType.COMMENT);

            given(notificationLogRepository.findByUserAndNotificationTypes(mockUser, types))
                    .willReturn(List.of());

            List<NotificationResponseDto> result = notificationLogService.getGeneralNotification(mockUser);

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(notificationLogRepository).findByUserAndNotificationTypes(mockUser, types);
        }
    }


    @Nested
    @DisplayName("readNotification() 테스트")
    class ReadNotificationTest {

        private NotificationLog unreadLog;
        private String notificationLogId;

        @BeforeEach
        void setUpReadNotification() throws Exception {
            Notification notification = Notification.of(mockUser, "제목", "본문", NoticeType.CEREMONY, "targetId");
            unreadLog = NotificationLog.of(mockUser, notification); // isRead = false 초기값

            Field idField = unreadLog.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(unreadLog, "log-id-123");
            notificationLogId = "log-id-123";
        }

        @Test
        @DisplayName("정상적으로 읽음 처리")
        void readNotificationSuccess() {
            given(notificationLogRepository.findByIdAndUser(notificationLogId, mockUser))
                    .willReturn(java.util.Optional.of(unreadLog));

            notificationLogService.readNotification(mockUser, notificationLogId);

            assertThat(unreadLog.getIsRead()).isTrue();
            verify(notificationLogRepository).findByIdAndUser(notificationLogId, mockUser);
        }

        @Test
        @DisplayName("알림 로그가 존재하지 않으면 예외 발생")
        void readNotificationNotFound() {
            given(notificationLogRepository.findByIdAndUser("non-existent-id", mockUser))
                    .willReturn(java.util.Optional.empty());

            org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                            notificationLogService.readNotification(mockUser, "non-existent-id"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining(MessageUtil.NOTIFICATION_LOG_NOT_FOUND);

            verify(notificationLogRepository).findByIdAndUser("non-existent-id", mockUser);
        }
    }



}
