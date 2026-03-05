package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.v2.event.AdmissionAcceptedEvent;
import net.causw.app.main.domain.notification.notification.service.v2.event.AdmissionRejectedEvent;
import net.causw.app.main.domain.notification.notification.service.v2.event.AdmissionRequestedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdmissionNotificationHandler {

	private final UserReader userReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final NotificationSettingReader notificationSettingReader;
	private final UserBlockEntityService userBlockEntityService;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handleRequest(AdmissionRequestedEvent event) {
		User requester = event.requester();

		List<User> admins = userReader.findAdminsByAcademicStatus(event.targetStatus());
		if (admins.isEmpty()) {
			return;
		}

		Set<String> blockedByRequester = userBlockEntityService.findBlockeeUserIdsByBlocker(requester);

		List<String> adminIds = admins.stream().map(User::getId).collect(Collectors.toList());
		Map<String, UserNotificationSettingMap> settingMaps = notificationSettingReader
			.findSettingMapByUserIds(adminIds);

		String title = "재학정보 인증 요청";
		String body = String.format("%s(%s)님이 재학정보 인증을 요청했습니다.", requester.getName(), requester.getStudentId());

		Notification notification = notificationWriter.save(
			Notification.of(requester, title, body, NoticeType.ADMISSION, null, null));

		admins.stream()
			.filter(admin -> !blockedByRequester.contains(admin.getId()))
			.filter(admin -> settingMaps.get(admin.getId()).get(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED))
			.forEach(admin -> {
				notificationPushSender.sendToUser(admin, title, body);
				notificationWriter.saveLog(admin, notification);
			});
	}

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handleAccepted(AdmissionAcceptedEvent event) {
		User targetUser = event.targetUser();

		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(targetUser.getId());
		if (!settingMap.get(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED)) {
			return;
		}

		String title = "재학정보 인증 완료";
		String body = "재학정보 인증이 완료되었습니다.";

		Notification notification = notificationWriter.save(
			Notification.of(targetUser, title, body, NoticeType.ADMISSION, null, null));

		notificationPushSender.sendToUser(targetUser, title, body);
		notificationWriter.saveLog(targetUser, notification);
	}

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handleRejected(AdmissionRejectedEvent event) {
		User targetUser = event.targetUser();

		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(targetUser.getId());
		if (!settingMap.get(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED)) {
			return;
		}

		String title = "재학정보 인증 반려";
		String body = String.format("재학정보 인증이 반려되었습니다. 사유: %s", event.rejectMessage());

		Notification notification = notificationWriter.save(
			Notification.of(targetUser, title, body, NoticeType.ADMISSION, null, null));

		notificationPushSender.sendToUser(targetUser, title, body);
		notificationWriter.saveLog(targetUser, notification);
	}
}
