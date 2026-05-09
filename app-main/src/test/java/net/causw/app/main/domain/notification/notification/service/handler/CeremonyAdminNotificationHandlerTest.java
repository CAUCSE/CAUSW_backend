package net.causw.app.main.domain.notification.notification.service.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.CeremonyAdminNotificationEvent;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;

@ExtendWith(MockitoExtension.class)
class CeremonyAdminNotificationHandlerTest {

	@InjectMocks
	private CeremonyAdminNotificationHandler handler;

	@Mock
	private CeremonyReader ceremonyReader;
	@Mock
	private UserReader userReader;
	@Mock
	private NotificationWriter notificationWriter;
	@Mock
	private NotificationPushSender notificationPushSender;
	@Mock
	private NotificationSettingReader notificationSettingReader;

	@Nested
	@DisplayName("경조사 신청 관리자 알림 (handle)")
	class HandleTest {

		@Test
		@DisplayName("성공: 신청자 학적과 동일한 관리자 중 서비스 알림 ON인 대상에게 발송")
		void givenAdminsWithNotificationOn_whenHandle_thenSendToAdmins() {
			// given
			User applicant = mock(User.class);
			given(applicant.getName()).willReturn("김신청");
			given(applicant.getAcademicStatus()).willReturn(AcademicStatus.ENROLLED);

			Ceremony ceremony = mock(Ceremony.class);
			given(ceremony.getId()).willReturn("ceremonyId");
			given(ceremony.getUser()).willReturn(applicant);

			User admin1 = userWithId("adminId1");
			User admin2 = userWithId("adminId2");
			List<User> admins = List.of(admin1, admin2);

			Notification savedNotification = mock(Notification.class);

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(userReader.findAdminsByAcademicStatus(AcademicStatus.ENROLLED)).willReturn(admins);
			given(notificationSettingReader.findSettingMapByUserIds(List.of("adminId1", "adminId2")))
				.willReturn(Map.of(
					"adminId1", settingMapAllOn(),
					"adminId2", settingMapAllOn()));
			given(notificationWriter.save(any())).willReturn(savedNotification);

			// when
			handler.handle(new CeremonyAdminNotificationEvent("ceremonyId"));

			// then
			verify(userReader).findAdminsByAcademicStatus(AcademicStatus.ENROLLED);

			ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
			verify(notificationWriter).save(notificationCaptor.capture());
			Notification captured = notificationCaptor.getValue();
			assertThat(captured.getNoticeType()).isEqualTo(NoticeType.SYSTEM);
			assertThat(captured.getTargetId()).isEqualTo("ceremonyId");

			verify(notificationPushSender).sendToUsers(
				eq(List.of(admin1, admin2)),
				eq("경조사 신청"),
				eq("김신청님이 경조사를 신청했습니다."));
			verify(notificationWriter).saveLogs(eq(List.of(admin1, admin2)), eq(savedNotification));
		}

		@Test
		@DisplayName("스킵: 신청자 학적에 맞는 관리자가 없으면 저장·발송하지 않음")
		void givenNoAdmins_whenHandle_thenSkip() {
			// given
			User applicant = mock(User.class);
			given(applicant.getAcademicStatus()).willReturn(AcademicStatus.GRADUATED);

			Ceremony ceremony = mock(Ceremony.class);
			given(ceremony.getUser()).willReturn(applicant);

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(userReader.findAdminsByAcademicStatus(AcademicStatus.GRADUATED)).willReturn(List.of());

			// when
			handler.handle(new CeremonyAdminNotificationEvent("ceremonyId"));

			// then
			verify(notificationWriter, never()).save(any());
			verify(notificationPushSender, never()).sendToUsers(any(), any(), any());
		}

		@Test
		@DisplayName("스킵: 서비스 알림 설정 OFF인 관리자는 푸시·로그 제외")
		void givenAdminWithNotificationOff_whenHandle_thenExcludeAdmin() {
			// given
			User applicant = mock(User.class);
			given(applicant.getName()).willReturn("이신청");
			given(applicant.getAcademicStatus()).willReturn(AcademicStatus.GRADUATED);

			Ceremony ceremony = mock(Ceremony.class);
			given(ceremony.getId()).willReturn("ceremonyId");
			given(ceremony.getUser()).willReturn(applicant);

			User adminOn = userWithId("adminOnId");
			User adminOff = userWithId("adminOffId");
			List<User> admins = List.of(adminOn, adminOff);

			Notification savedNotification = mock(Notification.class);

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(userReader.findAdminsByAcademicStatus(AcademicStatus.GRADUATED)).willReturn(admins);
			given(notificationSettingReader.findSettingMapByUserIds(List.of("adminOnId", "adminOffId")))
				.willReturn(Map.of(
					"adminOnId", settingMapAllOn(),
					"adminOffId", settingMapWith(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED, false)));
			given(notificationWriter.save(any())).willReturn(savedNotification);

			// when
			handler.handle(new CeremonyAdminNotificationEvent("ceremonyId"));

			// then
			verify(notificationPushSender).sendToUsers(eq(List.of(adminOn)), any(), any());
			verify(notificationWriter).saveLogs(eq(List.of(adminOn)), eq(savedNotification));
		}

		@Test
		@DisplayName("실패: 경조사가 없으면 NOT_FOUND 예외")
		void givenNotFoundCeremony_whenHandle_thenThrowException() {
			// given
			given(ceremonyReader.findById("missingId")).willReturn(Optional.empty());

			// when
			assertThatThrownBy(() -> handler.handle(new CeremonyAdminNotificationEvent("missingId")))
				.isInstanceOf(BaseRunTimeV2Exception.class);

			// then
			verify(userReader, never()).findAdminsByAcademicStatus(any());
		}
	}

	// ─────────────────────────────────────────────────
	// 헬퍼
	// ─────────────────────────────────────────────────

	private User userWithId(String id) {
		User user = mock(User.class);
		given(user.getId()).willReturn(id);
		return user;
	}

	private UserNotificationSettingMap settingMapAllOn() {
		return UserNotificationSettingMap.ofFull(Map.of());
	}

	private UserNotificationSettingMap settingMapWith(UserNotificationSettingKey key, boolean value) {
		return UserNotificationSettingMap.ofFull(Map.of(key, value));
	}
}
