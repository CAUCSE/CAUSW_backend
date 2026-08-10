package net.causw.app.main.domain.asset.locker.service.implementation;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.audit.event.AdminAuditLogEventPublisher;
import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.notification.notification.event.LockerExpiredEvent;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LockerExpirationProcessor {

	private final LockerReader lockerReader;
	private final LockerWriter lockerWriter;
	private final AdminAuditLogEventPublisher adminAuditLogEventPublisher;
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * 만료 사물함 단건 반납 처리 (독립 트랜잭션)
	 * <br> 락 획득 후 만료 상태를 재검증하여 이미 반납되었거나 만료되지 않은 사물함은 스킵한다. (멱등성 보장)
	 * @param lockerId 사물함 아이디
	 * @param actor 행위자 (관리자 또는 배치 시스템 계정)
	 * @return 반납 처리 여부 (스킵 시 false)
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean releaseExpiredLocker(String lockerId, User actor) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);

		if (!locker.isExpired(LocalDateTime.now())) {
			return false;
		}

		var expiredUser = locker.getUser();
		var userId = expiredUser.map(User::getId).orElse(null);
		var userEmail = expiredUser.map(User::getEmail).orElse("알 수 없음");
		var userName = expiredUser.map(User::getName).orElse("알 수 없음");

		lockerWriter.releaseLocker(locker, actor, userEmail, userName);

		adminAuditLogEventPublisher.publishLockerReleaseExpired(locker, actor, expiredUser);
		if (userId != null) {
			applicationEventPublisher.publishEvent(new LockerExpiredEvent(userId, locker.getId()));
		}
		return true;
	}
}
