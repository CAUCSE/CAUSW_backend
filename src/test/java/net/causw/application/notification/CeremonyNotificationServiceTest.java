package net.causw.application.notification;

import com.google.firebase.messaging.FirebaseMessagingException;
import net.causw.adapter.persistence.ceremony.Ceremony;
import net.causw.adapter.persistence.notification.CeremonyNotificationSetting;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.repository.notification.CeremonyNotificationSettingRepository;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.user.UserCreateRequestDto;
import net.causw.domain.model.enums.ceremony.CeremonyCategory;
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
class CeremonyNotificationServiceTest {

    @InjectMocks
    private CeremonyNotificationService ceremonyNotificationService;

    @Mock
    private FirebasePushNotificationService firebasePushNotificationService;

    @Mock
    private CeremonyNotificationSettingRepository ceremonyNotificationSettingRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    private User mockUser;
    private Ceremony mockCeremony;

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
        mockUser.getFcmTokens().add("valid-token");

        mockCeremony = mock(Ceremony.class);
        given(mockCeremony.getId()).willReturn("ceremony-id");
        given(mockCeremony.getUser()).willReturn(mockUser);
        given(mockCeremony.getCeremonyCategory()).willReturn(CeremonyCategory.MARRIAGE);
    }

    @Test
    @DisplayName("경조사 알림설정 있는 경우 - 경조사 알림 정상 전송 및 로그 저장")
    void sendByAdmissionYear_설정있음()  {
        given(ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(2023))
                .willReturn(List.of(CeremonyNotificationSetting.of(Set.of(2023, 2024), true, true, mockUser)));

        ceremonyNotificationService.sendByAdmissionYear(2023, mockCeremony);

        verify(notificationRepository).save(any(Notification.class));
        verify(notificationLogRepository).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("경조사 알림 설정이 없는 경우 - 경조사 알림 저장, 전송 없음, 로그 저장 없음")
    void sendByAdmissionYear_설정없음() throws Exception{
        given(ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(2023))
                .willReturn(List.of());

        ceremonyNotificationService.sendByAdmissionYear(2023, mockCeremony);

        verify(notificationRepository).save(any(Notification.class));
        verify(notificationLogRepository, never()).save(any(NotificationLog.class));
        verify(firebasePushNotificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    @DisplayName("비정상 토큰일 경우 푸시 알림 실패, 토큰 제거")
    void sendByAdmissionYear_푸시실패_토큰제거() throws Exception {
        String invalidToken = "invalid-token";
        mockUser.getFcmTokens().add(invalidToken);

        given(ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(2023))
                .willReturn(List.of(CeremonyNotificationSetting.of(Set.of(2023, 2024), true, true, mockUser)));

        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        doThrow(exception)
                .when(firebasePushNotificationService)
                .sendNotification(eq(invalidToken), any(), any());

        ceremonyNotificationService.sendByAdmissionYear(2023, mockCeremony);

        assertThat(mockUser.getFcmTokens()).doesNotContain(invalidToken);
        verify(firebasePushNotificationService).sendNotification(eq(invalidToken), any(), any());
    }

}
