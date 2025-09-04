package net.causw.app.main.service.notification;

import com.google.firebase.messaging.FirebaseMessagingException;

import net.causw.app.main.domain.model.entity.ceremony.Ceremony;
import net.causw.app.main.domain.model.entity.notification.CeremonyNotificationSetting;
import net.causw.app.main.domain.model.entity.notification.Notification;
import net.causw.app.main.domain.model.entity.notification.NotificationLog;
import net.causw.app.main.infrastructure.firebase.FcmUtils;
import net.causw.app.main.repository.ceremony.CeremonyRepository;
import net.causw.app.main.repository.notification.CeremonyNotificationSettingRepository;
import net.causw.app.main.repository.notification.NotificationLogRepository;
import net.causw.app.main.repository.notification.NotificationRepository;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.user.UserCreateRequestDto;
import net.causw.app.main.domain.model.enums.ceremony.CeremonyCategory;
import net.causw.app.main.service.userBlock.UserBlockEntityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
	UserBlockEntityService userBlockEntityService;

	@Mock
	private CeremonyNotificationSettingRepository ceremonyNotificationSettingRepository;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private NotificationLogRepository notificationLogRepository;

	@Mock
	private CeremonyRepository ceremonyRepository;

	@Mock
	private FcmUtils fcmUtils;

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
		given(mockCeremony.getStartDate()).willReturn(LocalDate.of(2024, 4, 15));
		given(mockCeremony.getEndDate()).willReturn(LocalDate.of(2024, 4, 16));
		given(mockCeremony.getCeremonyCategory()).willReturn(CeremonyCategory.MARRIAGE);
		given(mockCeremony.isSetAll()).willReturn(true);
		given(ceremonyRepository.findById("ceremony-id")).willReturn(Optional.of(mockCeremony));
	}

	@Test
	@DisplayName("경조사 알림설정 있는 경우 - 경조사 알림 정상 전송 및 로그 저장")
	void sendByAdmissionYear_설정있음() {
		Set<String> blockerUserIds = Set.of();
		given(ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(2023, blockerUserIds))
			.willReturn(List.of(CeremonyNotificationSetting.of(Set.of("23", "24"), true, true, mockUser)));

		ceremonyNotificationService.sendByAdmissionYear(2023, mockCeremony.getId());

		verify(notificationRepository).save(any(Notification.class));
		verify(notificationLogRepository).save(any(NotificationLog.class));
	}

	@Test
	@DisplayName("경조사 알림 설정이 없는 경우 - 경조사 알림 저장, 전송 없음, 로그 저장 없음")
	void sendByAdmissionYear_설정없음() throws Exception {
		Set<String> blockerUserIds = Set.of();
		given(ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(2023, blockerUserIds))
			.willReturn(List.of());

		ceremonyNotificationService.sendByAdmissionYear(2023, mockCeremony.getId());

		verify(notificationRepository).save(any(Notification.class));
		verify(notificationLogRepository, never()).save(any(NotificationLog.class));
		verify(firebasePushNotificationService, never()).sendNotification(any(), any(), any());
	}

	@Test
	@DisplayName("정상 토큰일 경우 푸시 알림 전송 성공")
	void sendByAdmissionYear_푸시성공() throws Exception {
		String validToken = "valid-token";
		Set<String> fcmTokens = new HashSet<>();
		fcmTokens.add(validToken);
		mockUser.setFcmTokens(fcmTokens);

		Set<String> blockerUserIds = Set.of();

		given(ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(2023, blockerUserIds))
			.willReturn(List.of(CeremonyNotificationSetting.of(Set.of("23", "24"), true, true, mockUser)));

		ceremonyNotificationService.sendByAdmissionYear(2023, mockCeremony.getId());

		verify(firebasePushNotificationService).sendNotification(validToken, "테스트 유저(2023) - 결혼식",
			"기간 : 2024-04-15 ~ 2024-04-16");
		verify(notificationLogRepository).save(any(NotificationLog.class));
	}

	@Test
	@DisplayName("비정상 토큰일 경우 푸시 알림 실패, 토큰 제거")
	void sendByAdmissionYear_푸시실패_토큰제거() throws Exception {
		String invalidToken = "invalid-token";

		Set<String> fcmTokens = new HashSet<>(Set.of("valid-token", invalidToken));
		mockUser.setFcmTokens(fcmTokens);

		Set<String> blockerUserIds = Set.of();

		given(ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(2023, blockerUserIds))
			.willReturn(List.of(CeremonyNotificationSetting.of(Set.of("23", "24"), true, true, mockUser)));

		FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
		doThrow(exception)
			.when(firebasePushNotificationService)
			.sendNotification(eq(invalidToken), any(), any());

		doAnswer(invocation -> {
			User user = invocation.getArgument(0);
			String token = invocation.getArgument(1);
			user.removeFcmToken(token);
			return null;
		}).when(fcmUtils).removeFcmToken(any(User.class), anyString());

		ceremonyNotificationService.sendByAdmissionYear(2023, mockCeremony.getId());

		assertThat(mockUser.getFcmTokens()).doesNotContain(invalidToken);
		assertThat(mockUser.getFcmTokens()).containsExactly("valid-token");
		verify(firebasePushNotificationService).sendNotification(eq("valid-token"), any(), any());
		verify(firebasePushNotificationService).sendNotification(eq(invalidToken), any(), any());
	}

}
