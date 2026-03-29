package net.causw.app.main.domain.notification.notification.service.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyCategory;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;
import net.causw.app.main.domain.community.ceremony.enums.RelationType;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.CeremonyNotificationEvent;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;

@ExtendWith(MockitoExtension.class)
class CeremonyNotificationHandlerTest {

	@InjectMocks
	private CeremonyNotificationHandler handler;

	@Mock
	private CeremonyReader ceremonyReader;
	@Mock
	private NotificationWriter notificationWriter;
	@Mock
	private NotificationPushSender notificationPushSender;
	@Mock
	private NotificationSettingReader notificationSettingReader;
	@Mock
	private UserReader userReader;

	@Nested
	@DisplayName("경조사 알림 (handle)")
	class HandleTest {

		@Test
		@DisplayName("성공: isSetAll=true이면 전체 활성 유저 대상으로 알림 발송")
		void givenSetAll_whenHandle_thenSendToAllActiveUsers() {
			// given
			Ceremony ceremony = celebrationSetAll();
			User target1 = userWithId("user1");
			User target2 = userWithId("user2");
			List<User> allUsers = List.of(target1, target2);

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(userReader.findAllActive()).willReturn(allUsers);
			given(notificationSettingReader.findSettingMapByUserIds(List.of("user1", "user2")))
				.willReturn(Map.of(
					"user1", settingMapAllOn(),
					"user2", settingMapAllOn()));
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new CeremonyNotificationEvent("ceremonyId"));

			// then
			verify(userReader).findAllActive();
			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUsers(any(), any(), any());
			verify(notificationWriter).saveLogs(any(), any());
		}

		@Test
		@DisplayName("성공: isSetAll=false이면 대상 입학년도 유저에게만 알림 발송")
		void givenTargetYears_whenHandle_thenSendToTargetYearUsers() {
			// given
			Ceremony ceremony = celebrationWithTargetYears(Set.of("24", "25")); // 2024, 2025
			User target = userWithId("userId");

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(userReader.findUsersByAdmissionYears(any())).willReturn(List.of(target));
			given(notificationSettingReader.findSettingMapByUserIds(List.of("userId")))
				.willReturn(Map.of("userId", settingMapAllOn()));
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new CeremonyNotificationEvent("ceremonyId"));

			// then
			verify(userReader).findUsersByAdmissionYears(any());
			verify(notificationPushSender).sendToUsers(any(), any(), any());
		}

		@Test
		@DisplayName("스킵: 경조사 알림 설정 OFF인 유저는 발송 제외")
		void givenCeremonyNotificationOff_whenHandle_thenExcludeUser() {
			// given
			Ceremony ceremony = celebrationSetAll();
			User userOn = userWithId("userOnId");
			User userOff = userWithId("userOffId");

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(userReader.findAllActive()).willReturn(List.of(userOn, userOff));
			given(notificationSettingReader.findSettingMapByUserIds(List.of("userOnId", "userOffId")))
				.willReturn(Map.of(
					"userOnId", settingMapAllOn(),
					"userOffId", settingMapWith(UserNotificationSettingKey.CEREMONY_NOTIFICATION_ENABLED, false)));
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new CeremonyNotificationEvent("ceremonyId"));

			// then: userOff는 targets에서 제외 → saveLogs에 userOn만 포함
			verify(notificationWriter).saveLogs(eq(List.of(userOn)), any());
		}

		@Test
		@DisplayName("실패: 존재하지 않는 경조사 ID이면 예외 발생")
		void givenNotFoundCeremony_whenHandle_thenThrowException() {
			// given
			given(ceremonyReader.findById("notExistId")).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> handler.handle(new CeremonyNotificationEvent("notExistId")))
				.isInstanceOf(BaseRunTimeV2Exception.class);
		}

		@Test
		@DisplayName("성공: 조사(CONDOLENCE)이면 푸시 타이틀이 '조사 소식'")
		void givenCondolenceCeremony_whenHandle_thenPushTitleIsCondolence() {
			// given
			Ceremony ceremony = condolenceSetAll();
			User target = userWithId("userId");

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(userReader.findAllActive()).willReturn(List.of(target));
			given(notificationSettingReader.findSettingMapByUserIds(List.of("userId")))
				.willReturn(Map.of("userId", settingMapAllOn()));
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new CeremonyNotificationEvent("ceremonyId"));

			// then
			verify(notificationPushSender).sendToUsers(any(), eq("조사 소식"), any());
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

	/** isSetAll=true, 경사(CELEBRATION), 본인(ME) */
	private Ceremony celebrationSetAll() {
		User ceremonyUser = mock(User.class);
		given(ceremonyUser.getName()).willReturn("홍길동");

		Ceremony ceremony = mock(Ceremony.class);
		given(ceremony.getId()).willReturn("ceremonyId");
		given(ceremony.getUser()).willReturn(ceremonyUser);
		given(ceremony.isSetAll()).willReturn(true);
		given(ceremony.getCeremonyType()).willReturn(CeremonyType.CELEBRATION);
		given(ceremony.getCeremonyCategory()).willReturn(CeremonyCategory.MARRIAGE);
		given(ceremony.getRelationType()).willReturn(RelationType.ME);
		given(ceremony.getAddress()).willReturn("서울시 강남구");
		given(ceremony.getStartDate()).willReturn(LocalDate.of(2025, 5, 10));
		given(ceremony.getStartTime()).willReturn(LocalTime.of(14, 0));
		return ceremony;
	}

	/** isSetAll=false, 특정 입학년도 대상 */
	private Ceremony celebrationWithTargetYears(Set<String> targetYears) {
		Ceremony ceremony = celebrationSetAll();
		given(ceremony.isSetAll()).willReturn(false);
		given(ceremony.getTargetAdmissionYears()).willReturn(targetYears);
		return ceremony;
	}

	/** 조사(CONDOLENCE, FUNERAL), isSetAll=true */
	private Ceremony condolenceSetAll() {
		User ceremonyUser = mock(User.class);
		given(ceremonyUser.getName()).willReturn("홍길동");

		Ceremony ceremony = mock(Ceremony.class);
		given(ceremony.getId()).willReturn("ceremonyId");
		given(ceremony.getUser()).willReturn(ceremonyUser);
		given(ceremony.isSetAll()).willReturn(true);
		given(ceremony.getCeremonyType()).willReturn(CeremonyType.CONDOLENCE);
		given(ceremony.getCeremonyCategory()).willReturn(CeremonyCategory.FUNERAL);
		given(ceremony.getRelationType()).willReturn(RelationType.ME);
		given(ceremony.getAddress()).willReturn("빈소 주소");
		given(ceremony.getEndDate()).willReturn(LocalDate.of(2025, 5, 12));
		// FUNERAL 분기는 getStartDate()를 호출하지 않으므로 stub 생략
		return ceremony;
	}

	private UserNotificationSettingMap settingMapAllOn() {
		return UserNotificationSettingMap.ofFull(Map.of());
	}

	private UserNotificationSettingMap settingMapWith(UserNotificationSettingKey key, boolean value) {
		return UserNotificationSettingMap.ofFull(Map.of(key, value));
	}
}
