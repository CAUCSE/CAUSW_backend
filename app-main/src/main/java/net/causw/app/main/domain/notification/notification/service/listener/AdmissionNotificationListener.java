package net.causw.app.main.domain.notification.notification.service.listener;

import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
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

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdmissionNotificationListener {

	private final UserReader userReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final NotificationSettingReader notificationSettingReader;

	/**
	 * 재학정보 인증 요청 알림 이벤트 핸들러.
	 * <p>
	 * 유저가 재학정보 인증을 요청하면, 해당 학적 상태를 처리하는 관리자들에게
	 * 푸시 알림 및 서비스 알림을 발송합니다.
	 * <ul>
	 *   <li>대상: {@code event.targetStatus()} 에 해당하는 학적 상태의 관리자</li>
	 *   <li>필터: 서비스 알림 설정 ON ({@link UserNotificationSettingKey#SERVICE_NOTICE_ENABLED})</li>
	 * </ul>
	 *
	 * @param event 재학정보 인증 요청 이벤트
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleRequest(AdmissionRequestedEvent event) {
		// ID로 요청자 조회
		User requester = userReader.findUserById(event.requesterId());

		// 해당 학적 상태를 담당하는 관리자 목록 조회
		List<User> admins = userReader.findAdminsByAcademicStatus(event.targetStatus());
		if (admins.isEmpty()) {
			return;
		}

		// 관리자별 알림 설정 일괄 조회
		List<String> adminIds = admins.stream().map(User::getId).toList();
		Map<String, UserNotificationSettingMap> settingMaps = notificationSettingReader
			.findSettingMapByUserIds(adminIds);

		String pushTitle = "재학정보 인증 요청";
		String studentId = AcademicStatus.GRADUATED.equals(event.targetStatus()) ? "졸업생" : event.requestStudentId();
		String message = String.format("%s(%s)님이 재학정보 인증을 요청했습니다.", requester.getName(), studentId);

		String pushBody = message;
		String serviceTitle = message;

		// 알림 엔티티 저장 (발송자: 요청자)
		Notification notification = notificationWriter.save(
			Notification.of(requester, serviceTitle, serviceTitle, NoticeType.SYSTEM, null, null));

		// 서비스 알림이 활성화된 관리자에게만 발송
		admins.stream()
			.filter(admin -> settingMaps.get(admin.getId()).get(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED))
			.forEach(admin -> {
				notificationPushSender.sendToUser(admin, pushTitle, pushBody);
				notificationWriter.saveLog(admin, notification);
			});
	}

	/**
	 * 재학정보 인증 승인 알림 이벤트 핸들러.
	 * <p>
	 * 관리자가 재학정보 인증을 승인하면, 해당 유저에게 푸시 알림 및 서비스 알림을 발송합니다.
	 * <ul>
	 *   <li>필터: 서비스 알림 설정 ON ({@link UserNotificationSettingKey#SERVICE_NOTICE_ENABLED})</li>
	 * </ul>
	 *
	 * @param event 재학정보 인증 승인 이벤트
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleAccepted(AdmissionAcceptedEvent event) {
		// ID로 관리자·대상 유저 조회
		User admin = userReader.findUserById(event.adminId());
		User targetUser = userReader.findUserById(event.targetUserId());

		// 서비스 알림 설정 확인
		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(targetUser.getId());
		if (!settingMap.get(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED)) {
			return;
		}

		String title = "재학정보 인증 완료";
		String body = "재학정보 인증이 완료되었습니다.";

		Notification notification = notificationWriter.save(
			Notification.of(admin, title, body, NoticeType.SYSTEM, null, null));

		notificationPushSender.sendToUser(targetUser, title, body);
		notificationWriter.saveLog(targetUser, notification);
	}

	/**
	 * 재학정보 인증 반려 알림 이벤트 핸들러.
	 * <p>
	 * 관리자가 재학정보 인증을 반려하면, 해당 유저에게 반려 사유와 함께
	 * 푸시 알림 및 서비스 알림을 발송합니다.
	 * <ul>
	 *   <li>필터: 서비스 알림 설정 ON ({@link UserNotificationSettingKey#SERVICE_NOTICE_ENABLED})</li>
	 * </ul>
	 *
	 * @param event 재학정보 인증 반려 이벤트 (반려 사유 포함)
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleRejected(AdmissionRejectedEvent event) {
		// ID로 관리자·대상 유저 조회
		User admin = userReader.findUserById(event.adminId());
		User targetUser = userReader.findUserById(event.targetUserId());

		// 서비스 알림 설정 확인
		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(targetUser.getId());
		if (!settingMap.get(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED)) {
			return;
		}

		String title = "재학정보 인증 반려";
		String body = String.format("재학정보 인증이 반려되었습니다. 사유: %s", event.rejectMessage());

		Notification notification = notificationWriter.save(
			Notification.of(admin, title, body, NoticeType.SYSTEM, null, null));

		notificationPushSender.sendToUser(targetUser, title, body);
		notificationWriter.saveLog(targetUser, notification);
	}
}
