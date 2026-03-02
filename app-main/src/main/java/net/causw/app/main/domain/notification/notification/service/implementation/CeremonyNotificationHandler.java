package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.repository.v1.CeremonyV1Repository;
import net.causw.app.main.domain.notification.notification.entity.CeremonyNotificationSetting;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.repository.CeremonyNotificationSettingRepository;
import net.causw.app.main.domain.notification.notification.service.v2.event.CeremonyNotificationEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CeremonyNotificationHandler {

	private final CeremonyV1Repository ceremonyV1Repository;
	private final CeremonyNotificationSettingRepository ceremonyNotificationSettingRepository;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final UserBlockEntityService userBlockEntityService;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handle(CeremonyNotificationEvent event) {
		Ceremony ceremony = ceremonyV1Repository.findById(event.ceremonyId())
			.orElseThrow(CeremonyErrorCode.CEREMONY_NOT_FOUND::toBaseException);

		User ceremonyUser = ceremony.getUser();
		Set<String> blockerUserIds = userBlockEntityService.findBlockerUserIdsByBlockee(ceremonyUser);

		List<CeremonyNotificationSetting> targets = resolveTargets(ceremony, event.admissionYear(), blockerUserIds);

		String title = String.format("%s(%s) - %s",
			ceremonyUser.getName(),
			ceremonyUser.getAdmissionYear().toString(),
			ceremony.getCeremonyCategory().getLabel());
		String body = String.format("기간 : %s ~ %s",
			ceremony.getStartDate().toString(),
			ceremony.getEndDate().toString());

		Notification notification = notificationWriter.save(
			Notification.of(ceremonyUser, title, body, NoticeType.CEREMONY, ceremony.getId(), null));

		targets.stream()
			.map(CeremonyNotificationSetting::getUser)
			.forEach(user -> {
				notificationPushSender.sendToUser(user, title, body);
				notificationWriter.saveLog(user, notification);
			});
	}

	private List<CeremonyNotificationSetting> resolveTargets(Ceremony ceremony, Integer admissionYear,
		Set<String> blockerUserIds) {
		if (ceremony.isSetAll()) {
			return ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(admissionYear, blockerUserIds);
		}

		List<Integer> targetYears = ceremony.getTargetAdmissionYears().stream()
			.map(studentId -> {
				int year = Integer.parseInt(studentId);
				return year >= 72 ? 1900 + year : 2000 + year;
			})
			.collect(Collectors.toList());

		List<CeremonyNotificationSetting> filteredSettings = ceremonyNotificationSettingRepository
			.findByAdmissionYearsIn(targetYears, blockerUserIds);

		Integer ceremonyWriterYear = ceremony.getUser().getAdmissionYear();

		return filteredSettings.stream()
			.distinct()
			.filter(setting -> {
				if (!setting.isNotificationActive()) {
					return false;
				}
				if (setting.isSetAll()) {
					return true;
				}
				return setting.getSubscribedAdmissionYears().contains(ceremonyWriterYear);
			})
			.collect(Collectors.toList());
	}
}
