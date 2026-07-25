package net.causw.app.main.domain.notification.notification.service.listener;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

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
import net.causw.app.main.domain.notification.notification.event.CeremonyApprovedEvent;
import net.causw.app.main.domain.notification.notification.event.CeremonyNotificationEvent;
import net.causw.app.main.domain.notification.notification.event.CeremonyRejectedEvent;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;

@ExtendWith(MockitoExtension.class)
class CeremonyNotificationListenerTest {

	@InjectMocks
	private CeremonyNotificationListener handler;

	@Mock
	private CeremonyReader ceremonyReader;
	@Mock
	private NotificationWriter notificationWriter;
	@Mock
	private NotificationPushSender notificationPushSender;
	@Mock
	private NotificationSettingReader notificationSettingReader;

	@Nested
	@DisplayName("경조사 알림 (handle)")
	class HandleTest {

		@Test
		@DisplayName("성공: isSetAll=true이면 전체 활성 유저 대상으로 알림 발송")
		void givenSetAll_whenHandle_thenSendToAllActiveUsers() {
			// given
			Ceremony ceremony = celebrationSetAll();
			User target1 = mock(User.class);
			User target2 = mock(User.class);

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			// isSetAll=true → admissionYears=[] 로 전체 대상 쿼리
			given(notificationSettingReader.findCeremonyNotificationTargets(
				List.of(), UserNotificationSettingKey.CEREMONY_NOTIFICATION_ENABLED))
				.willReturn(List.of(target1, target2));
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new CeremonyNotificationEvent("ceremonyId"));

			// then
			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUsers(any(), any(), any());
			verify(notificationWriter).saveLogs(any(), any());
		}

		@Test
		@DisplayName("성공: isSetAll=false이면 대상 입학년도 유저에게만 알림 발송")
		void givenTargetYears_whenHandle_thenSendToTargetYearUsers() {
			// given
			Ceremony ceremony = celebrationWithTargetYears(Set.of(2024, 2025));
			User target = mock(User.class);

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			// isSetAll=false → admissionYears=[2024,2025] 로 대상 쿼리 (순서 무관하므로 any())
			given(notificationSettingReader.findCeremonyNotificationTargets(
				any(), eq(UserNotificationSettingKey.CEREMONY_NOTIFICATION_ENABLED)))
				.willReturn(List.of(target));
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new CeremonyNotificationEvent("ceremonyId"));

			// then
			verify(notificationPushSender).sendToUsers(any(), any(), any());
		}

		@Test
		@DisplayName("스킵: 경조사 알림 설정 OFF인 유저는 발송 제외")
		void givenCeremonyNotificationOff_whenHandle_thenExcludeUser() {
			// given
			Ceremony ceremony = celebrationSetAll();
			User userOn = mock(User.class);
			// userOff는 QueryRepository에서 제외되어 반환 목록에 포함되지 않음

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(notificationSettingReader.findCeremonyNotificationTargets(
				List.of(), UserNotificationSettingKey.CEREMONY_NOTIFICATION_ENABLED))
				.willReturn(List.of(userOn));
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new CeremonyNotificationEvent("ceremonyId"));

			// then: disabled 유저가 제외된 결과가 QueryRepository에서 반환됨
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
			User target = mock(User.class);

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(notificationSettingReader.findCeremonyNotificationTargets(
				List.of(), UserNotificationSettingKey.CEREMONY_NOTIFICATION_ENABLED))
				.willReturn(List.of(target));
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new CeremonyNotificationEvent("ceremonyId"));

			// then
			verify(notificationPushSender).sendToUsers(any(), eq("조사 소식"), any());
		}
	}

	@Nested
	@DisplayName("경조사 승인 결과 알림 (handleApproved)")
	class HandleApprovedTest {

		@Test
		@DisplayName("성공: SERVICE_NOTICE_ENABLED ON이면 신청자에게 푸시 + 서비스 알림 발송")
		void givenServiceNoticeOn_whenHandleApproved_thenSendToApplicant() {
			// given
			User applicant = mock(User.class);
			given(applicant.getId()).willReturn("userId");

			Ceremony ceremony = mock(Ceremony.class);
			given(ceremony.getId()).willReturn("ceremonyId");
			given(ceremony.getUser()).willReturn(applicant);

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(notificationSettingReader.findSettingMap("userId"))
				.willReturn(serviceNoticeOn());
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handleApproved(new CeremonyApprovedEvent("ceremonyId"));

			// then
			verify(notificationPushSender).sendToUser(eq(applicant), eq("경조사 신청 승인"), any());
			verify(notificationWriter).saveLog(eq(applicant), any());
		}

		@Test
		@DisplayName("스킵: SERVICE_NOTICE_ENABLED OFF이면 알림 발송하지 않음")
		void givenServiceNoticeOff_whenHandleApproved_thenSkip() {
			// given
			User applicant = mock(User.class);
			given(applicant.getId()).willReturn("userId");

			Ceremony ceremony = mock(Ceremony.class);
			given(ceremony.getUser()).willReturn(applicant);

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(notificationSettingReader.findSettingMap("userId"))
				.willReturn(serviceNoticeOff());

			// when
			handler.handleApproved(new CeremonyApprovedEvent("ceremonyId"));

			// then
			verify(notificationPushSender, never()).sendToUser(any(), any(), any());
			verify(notificationWriter, never()).saveLog(any(), any());
		}

	}

	@Nested
	@DisplayName("경조사 거절 결과 알림 (handleRejected)")
	class HandleRejectedTest {

		@Test
		@DisplayName("성공: SERVICE_NOTICE_ENABLED ON이면 신청자에게 푸시 발송하고 서비스 알림 제목에 거절 사유 포함")
		void givenServiceNoticeOn_whenHandleRejected_thenSendToApplicantAndIncludeRejectReason() {
			// given
			User applicant = mock(User.class);
			given(applicant.getId()).willReturn("userId");

			Ceremony ceremony = mock(Ceremony.class);
			given(ceremony.getId()).willReturn("ceremonyId");
			given(ceremony.getUser()).willReturn(applicant);

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(notificationSettingReader.findSettingMap("userId"))
				.willReturn(serviceNoticeOn());
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handleRejected(new CeremonyRejectedEvent("ceremonyId", "요건에 부합하지 않습니다."));

			// then
			verify(notificationPushSender).sendToUser(eq(applicant), eq("경조사 신청 거절"), eq("경조사 신청이 거절되었습니다."));
			verify(notificationWriter).save(argThat(n -> n.getTitle().contains("요건에 부합하지 않습니다.")));
			verify(notificationWriter).saveLog(eq(applicant), any());
		}

		@Test
		@DisplayName("스킵: SERVICE_NOTICE_ENABLED OFF이면 알림 발송하지 않음")
		void givenServiceNoticeOff_whenHandleRejected_thenSkip() {
			// given
			User applicant = mock(User.class);
			given(applicant.getId()).willReturn("userId");

			Ceremony ceremony = mock(Ceremony.class);
			given(ceremony.getUser()).willReturn(applicant);

			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));
			given(notificationSettingReader.findSettingMap("userId"))
				.willReturn(serviceNoticeOff());

			// when
			handler.handleRejected(new CeremonyRejectedEvent("ceremonyId", "사유"));

			// then
			verify(notificationPushSender, never()).sendToUser(any(), any(), any());
			verify(notificationWriter, never()).saveLog(any(), any());
		}

	}

	// ─────────────────────────────────────────────────
	// 헬퍼
	// ─────────────────────────────────────────────────

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
	private Ceremony celebrationWithTargetYears(Set<Integer> targetYears) {
		Ceremony ceremony = celebrationSetAll();
		given(ceremony.isSetAll()).willReturn(false);
		given(ceremony.getTargetAdmissionYears()).willReturn(targetYears);
		return ceremony;
	}

	private UserNotificationSettingMap serviceNoticeOn() {
		return UserNotificationSettingMap.ofFull(
			Map.of(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED, true));
	}

	private UserNotificationSettingMap serviceNoticeOff() {
		return UserNotificationSettingMap.ofFull(
			Map.of(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED, false));
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
}
