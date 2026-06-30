package net.causw.app.main.domain.notification.notification.service.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.asset.locker.service.implementation.LockerReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.event.LockerExpiredEvent;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LockerExpiredEventListener {

	private final LockerReader lockerReader;
	private final UserReader userReader;
	private final NotificationPushSender notificationPushSender;
	private final NotificationWriter notificationWriter;

	/**
	 * 반납 완료 시 사물함 반납 완료 안내
	 *
	 * @param event 사물함 만료 반납 이벤트
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(LockerExpiredEvent event) {
		Locker locker = lockerReader.findById(event.lockerId());
		LockerLocation location = locker.getLocation();
		User user = userReader.findUserById(event.userId());

		String description = String.format("이용기간이 만료되어 사물함(%s %d번)이 자동으로 반납되었습니다.", location.getDescription(),
			locker.getLockerNumber());
		Notification notification = Notification.of(user, description, description, NoticeType.LOCKER, locker.getId(),
			location.getId());

		notificationPushSender.sendToUser(user, description, description);
		notificationWriter.saveLog(user, notification);
	}
}
