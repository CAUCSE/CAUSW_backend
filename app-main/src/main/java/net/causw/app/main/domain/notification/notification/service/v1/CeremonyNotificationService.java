package net.causw.app.main.domain.notification.notification.service.v1;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.repository.v1.CeremonyV1Repository;
import net.causw.app.main.domain.notification.notification.api.v1.dto.CeremonyNotificationDto;
import net.causw.app.main.domain.notification.notification.entity.CeremonyNotificationSetting;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.repository.CeremonyNotificationSettingRepository;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.notification.notification.repository.NotificationRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;
import net.causw.app.main.shared.infra.firebase.FcmUtils;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import com.google.firebase.messaging.FirebaseMessagingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CeremonyNotificationService implements NotificationService {
	private final FirebasePushNotificationService firebasePushNotificationService;
	private final CeremonyNotificationSettingRepository ceremonyNotificationSettingRepository;
	private final NotificationRepository notificationRepository;
	private final NotificationLogRepository notificationLogRepository;
	private final FcmUtils fcmUtils;
	private final CeremonyV1Repository ceremonyV1Repository;
	private final UserBlockEntityService userBlockEntityService;

	@Override
	public void send(User user, String targetToken, String title, String body) {
		try {
			firebasePushNotificationService.sendNotification(targetToken, title, body);
		} catch (FirebaseMessagingException e) {
			log.warn("FCM 전송 실패: {}, 이유: {}", targetToken, e.getMessage());
			fcmUtils.removeFcmToken(user, targetToken);
			log.info("오류 발생으로 FCM 토큰 제거됨: {}", targetToken);
		} catch (Exception e) {
			log.error("FCM 전송 중 알 수 없는 예외 발생: {}", e.getMessage(), e);
		}
	}

	@Override
	public void saveNotification(Notification notification) {
		notificationRepository.save(notification);
	}

	@Override
	public void saveNotificationLog(User user, Notification notification) {
		notificationLogRepository.save(NotificationLog.of(user, notification));
	}

	@Async("asyncExecutor")
	@Transactional
	public void sendByAdmissionYear(Integer admissionYear, String ceremonyId) {
		Ceremony ceremony = ceremonyV1Repository.findById(ceremonyId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.CEREMONY_NOT_FOUND));

		List<CeremonyNotificationSetting> ceremonyNotificationSettings;

		User ceremonyUser = ceremony.getUser();

		Set<String> blockerUserIdsByBlockee = userBlockEntityService.findBlockerUserIdsByBlockee(ceremonyUser);

		if (ceremony.isSetAll()) {
			// 모든 학번에게 알림
			ceremonyNotificationSettings = ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(
				admissionYear,
				blockerUserIdsByBlockee);
		} else {
			// 특정 학번에게만 알림
			List<CeremonyNotificationSetting> filteredSettings = ceremonyNotificationSettingRepository
				.findByAdmissionYearsIn(ceremony.getTargetAdmissionYears().stream().toList(), blockerUserIdsByBlockee);

			// 2차 필터링
			ceremonyNotificationSettings = filteredSettings.stream()
				.distinct()
				.filter(setting -> {
					// 알림이 비활성화된 경우 제외
					if (!setting.isNotificationActive()) {
						return false;
					}

					// isSetAll이 true면 모든 경조사 수신
					if (setting.isSetAll()) {
						return true;
					}

					// 특정 입학년도만 수신
					Integer ceremonyWriterYear = ceremonyUser.getAdmissionYear();
					return setting.getSubscribedAdmissionYears().contains(ceremonyWriterYear);
				})
				.collect(Collectors.toList());
		}

		// 알림 생성 및 저장
		CeremonyNotificationDto ceremonyNotificationDto = CeremonyNotificationDto.of(ceremony);
		Notification notification = Notification.of(
			ceremonyUser,
			ceremonyNotificationDto.getTitle(),
			ceremonyNotificationDto.getBody(),
			NoticeType.CEREMONY,
			ceremony.getId(),
			null);

		saveNotification(notification);

		// 알림 전송
		ceremonyNotificationSettings.stream()
			.map(CeremonyNotificationSetting::getUser)
			.forEach(user -> {
				fcmUtils.cleanInvalidFcmTokens(user);
				Set<String> copy = new HashSet<>(user.getFcmTokens());
				copy.forEach(
					token -> send(user, token, ceremonyNotificationDto.getTitle(), ceremonyNotificationDto.getBody()));
				saveNotificationLog(user, notification);
			});
	}

}
