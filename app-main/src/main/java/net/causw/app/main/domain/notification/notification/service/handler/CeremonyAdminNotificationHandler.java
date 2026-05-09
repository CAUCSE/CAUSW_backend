package net.causw.app.main.domain.notification.notification.service.handler;

import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CeremonyAdminNotificationHandler {

	private final CeremonyReader ceremonyReader;
	private final UserReader userReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final NotificationSettingReader notificationSettingReader;

	/**
	 * 경조사 신청 관리자 알림 이벤트 핸들러.
	 * <p>
	 * 일반 유저가 경조사를 신청하면, 신청자와 동일한 {@link AcademicStatus}의 관리자에게
	 * 푸시 알림과 서비스 알림을 발송합니다.
	 * <ul>
	 *   <li>대상: 신청자 학적 상태와 일치하는 관리자</li>
	 *   <li>필터: 서비스 알림 설정 ON ({@link UserNotificationSettingKey#SERVICE_NOTICE_ENABLED})</li>
	 *   <li>처리: 푸시 전송 + 서비스 알림 로그 저장 ({@link NoticeType#SYSTEM})</li>
	 * </ul>
	 *
	 * @param event 경조사 관리자 알림 이벤트
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(CeremonyAdminNotificationEvent event) {
		Ceremony ceremony = ceremonyReader.findById(event.ceremonyId())
			.orElseThrow(CeremonyErrorCode.CEREMONY_NOT_FOUND::toBaseException);

		// 신청자와 동일 학적 상태의 관리자 조회
		List<User> admins = userReader.findAdminsByAcademicStatus(
			ceremony.getUser().getAcademicStatus());
		if (admins.isEmpty()) {
			return;
		}

		// 관리자별 알림 설정을 읽고, 알림 설정이 활성화된 대상만 필터링
		List<String> adminIds = admins.stream().map(User::getId).toList();
		Map<String, UserNotificationSettingMap> settingMaps = notificationSettingReader
			.findSettingMapByUserIds(adminIds);
		List<User> adminTargets = admins.stream()
			.filter(admin -> settingMaps.get(admin.getId()).get(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED))
			.toList();
		if (adminTargets.isEmpty()) {
			return;
		}

		String title = "경조사 신청";
		String body = String.format("%s님이 경조사를 신청했습니다.", ceremony.getUser().getName());

		// 서비스 알림함 저장용 Notification 엔티티 생성
		// UI에서 정보를 표시할 때 body 대신 title을 사용하므로, 바디와 같은 내용을 title에 저장
		Notification notification = notificationWriter.save(
			Notification.of(ceremony.getUser(), body, body, NoticeType.SYSTEM, ceremony.getId(), null));

		// 필터링된 관리자 대상에게 발송
		notificationPushSender.sendToUsers(adminTargets, title, body);
		notificationWriter.saveLogs(adminTargets, notification);
	}
}
