package net.causw.app.main.domain.notification.notification.service.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.AdmissionAcceptedEvent;
import net.causw.app.main.domain.notification.notification.event.AdmissionRejectedEvent;
import net.causw.app.main.domain.notification.notification.event.AdmissionRequestedEvent;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

@ExtendWith(MockitoExtension.class)
class AdmissionNotificationHandlerTest {

	@InjectMocks
	private AdmissionNotificationHandler handler;

	@Mock
	private UserReader userReader;
	@Mock
	private NotificationWriter notificationWriter;
	@Mock
	private NotificationPushSender notificationPushSender;
	@Mock
	private NotificationSettingReader notificationSettingReader;

	// ─────────────────────────────────────────────────
	// handleRequest
	// ─────────────────────────────────────────────────

	@Nested
	@DisplayName("재학정보 인증 요청 알림 (handleRequest)")
	class HandleRequestTest {

		@Test
		@DisplayName("성공: 알림 설정 ON인 관리자에게 알림 발송")
		void givenAdminsWithNotificationOn_whenHandleRequest_thenSendToAdmins() {
			// given
			User requester = mockUserWithId("requesterId", "홍길동", "20191234");
			User admin1 = mockUserWithId("adminId1", "관리자1", null);
			User admin2 = mockUserWithId("adminId2", "관리자2", null);
			List<User> admins = List.of(admin1, admin2);

			given(userReader.findUserById("requesterId")).willReturn(requester);
			given(userReader.findAdminsByAcademicStatus(AcademicStatus.ENROLLED)).willReturn(admins);
			given(notificationSettingReader.findSettingMapByUserIds(List.of("adminId1", "adminId2")))
				.willReturn(Map.of(
					"adminId1", settingMapAllOn(),
					"adminId2", settingMapAllOn()));
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handleRequest(new AdmissionRequestedEvent("requesterId", AcademicStatus.ENROLLED));

			// then
			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUser(admin1, any(), any());
			verify(notificationPushSender).sendToUser(admin2, any(), any());
		}

		@Test
		@DisplayName("스킵: 관리자 목록이 비어있으면 알림 미발송")
		void givenNoAdmins_whenHandleRequest_thenSkip() {
			// given
			User requester = mockUserWithId("requesterId", "홍길동", "20191234");
			given(userReader.findUserById("requesterId")).willReturn(requester);
			given(userReader.findAdminsByAcademicStatus(AcademicStatus.ENROLLED)).willReturn(List.of());

			// when
			handler.handleRequest(new AdmissionRequestedEvent("requesterId", AcademicStatus.ENROLLED));

			// then
			verify(notificationWriter, never()).save(any());
		}

		@Test
		@DisplayName("스킵: 알림 설정 OFF인 관리자는 발송 제외")
		void givenAdminWithNotificationOff_whenHandleRequest_thenExcludeAdmin() {
			// given
			User requester = mockUserWithId("requesterId", "홍길동", "20191234");
			User adminOn = mockUserWithId("adminOnId", "관리자On", null);
			User adminOff = mockUserWithId("adminOffId", "관리자Off", null);
			List<User> admins = List.of(adminOn, adminOff);

			given(userReader.findUserById("requesterId")).willReturn(requester);
			given(userReader.findAdminsByAcademicStatus(AcademicStatus.ENROLLED)).willReturn(admins);
			given(notificationSettingReader.findSettingMapByUserIds(List.of("adminOnId", "adminOffId")))
				.willReturn(Map.of(
					"adminOnId", settingMapAllOn(),
					"adminOffId", settingMapWith(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED, false)));
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handleRequest(new AdmissionRequestedEvent("requesterId", AcademicStatus.ENROLLED));

			// then
			verify(notificationPushSender).sendToUser(adminOn, any(), any());
			verify(notificationPushSender, never()).sendToUser(adminOff, any(), any());
		}
	}

	// ─────────────────────────────────────────────────
	// handleAccepted
	// ─────────────────────────────────────────────────

	@Nested
	@DisplayName("재학정보 인증 승인 알림 (handleAccepted)")
	class HandleAcceptedTest {

		@Test
		@DisplayName("성공: 알림 설정 ON이면 대상 유저에게 승인 알림 발송")
		void givenNotificationOn_whenHandleAccepted_thenSendToUser() {
			// given
			User admin = mockUserWithId("adminId", "관리자", null);
			User targetUser = mockUserWithId("targetUserId", "홍길동", "20191234");

			given(userReader.findUserById("adminId")).willReturn(admin);
			given(userReader.findUserById("targetUserId")).willReturn(targetUser);
			given(notificationSettingReader.findSettingMap("targetUserId")).willReturn(settingMapAllOn());
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handleAccepted(new AdmissionAcceptedEvent("adminId", "targetUserId"));

			// then
			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUser(any(), any(), any());
			verify(notificationWriter).saveLog(any(), any());
		}

		@Test
		@DisplayName("스킵: 대상 유저가 서비스 알림 설정 OFF면 알림 미발송")
		void givenNotificationOff_whenHandleAccepted_thenSkip() {
			// given
			User admin = mockUserWithId("adminId", "관리자", null);
			User targetUser = mockUserWithId("targetUserId", "홍길동", "20191234");

			given(userReader.findUserById("adminId")).willReturn(admin);
			given(userReader.findUserById("targetUserId")).willReturn(targetUser);
			given(notificationSettingReader.findSettingMap("targetUserId"))
				.willReturn(settingMapWith(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED, false));

			// when
			handler.handleAccepted(new AdmissionAcceptedEvent("adminId", "targetUserId"));

			// then
			verify(notificationWriter, never()).save(any());
			verify(notificationPushSender, never()).sendToUser(any(), any(), any());
		}
	}

	// ─────────────────────────────────────────────────
	// handleRejected
	// ─────────────────────────────────────────────────

	@Nested
	@DisplayName("재학정보 인증 반려 알림 (handleRejected)")
	class HandleRejectedTest {

		@Test
		@DisplayName("성공: 알림 설정 ON이면 대상 유저에게 반려 알림 발송")
		void givenNotificationOn_whenHandleRejected_thenSendToUser() {
			// given
			User admin = mockUserWithId("adminId", "관리자", null);
			User targetUser = mockUserWithId("targetUserId", "홍길동", "20191234");

			given(userReader.findUserById("adminId")).willReturn(admin);
			given(userReader.findUserById("targetUserId")).willReturn(targetUser);
			given(notificationSettingReader.findSettingMap("targetUserId")).willReturn(settingMapAllOn());
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handleRejected(new AdmissionRejectedEvent("adminId", "targetUserId", "증빙서류 불일치"));

			// then
			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUser(any(), any(), any());
			verify(notificationWriter).saveLog(any(), any());
		}

		@Test
		@DisplayName("스킵: 대상 유저가 서비스 알림 설정 OFF면 알림 미발송")
		void givenNotificationOff_whenHandleRejected_thenSkip() {
			// given
			User admin = mockUserWithId("adminId", "관리자", null);
			User targetUser = mockUserWithId("targetUserId", "홍길동", "20191234");

			given(userReader.findUserById("adminId")).willReturn(admin);
			given(userReader.findUserById("targetUserId")).willReturn(targetUser);
			given(notificationSettingReader.findSettingMap("targetUserId"))
				.willReturn(settingMapWith(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED, false));

			// when
			handler.handleRejected(new AdmissionRejectedEvent("adminId", "targetUserId", "증빙서류 불일치"));

			// then
			verify(notificationWriter, never()).save(any());
		}
	}

	// ─────────────────────────────────────────────────
	// 헬퍼
	// ─────────────────────────────────────────────────

	private User mockUserWithId(String id, String name, String studentId) {
		User user = mock(User.class);
		given(user.getId()).willReturn(id);
		if (name != null)
			given(user.getName()).willReturn(name);
		if (studentId != null)
			given(user.getStudentId()).willReturn(studentId);
		return user;
	}

	private UserNotificationSettingMap settingMapAllOn() {
		return UserNotificationSettingMap.ofFull(Map.of());
	}

	private UserNotificationSettingMap settingMapWith(UserNotificationSettingKey key, boolean value) {
		return UserNotificationSettingMap.ofFull(Map.of(key, value));
	}
}
