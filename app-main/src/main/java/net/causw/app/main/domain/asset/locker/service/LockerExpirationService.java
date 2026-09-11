package net.causw.app.main.domain.asset.locker.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.service.dto.result.LockerExpirationResult;
import net.causw.app.main.domain.asset.locker.service.implementation.LockerExpirationProcessor;
import net.causw.app.main.domain.asset.locker.service.implementation.LockerReader;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 만료 사물함 일괄 반납 서비스.
 *
 * <p>관리자 API와 스케줄러가 공유하는 만료 사물함 반납 로직을 제공한다.
 * 사물함 단위 트랜잭션으로 처리하여 한 건의 실패가 전체 롤백으로 이어지지 않으며,
 * 처리 건수와 실패 원인을 로그로 남긴다.</p>
 *
 * @see LockerAdminService 관리자용 사물함 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LockerExpirationService {

	private final LockerReader lockerReader;
	private final LockerExpirationProcessor lockerExpirationProcessor;

	/**
	 * 만료된 사물함 일괄 반납
	 * @param actor 행위자 (관리자 또는 배치 시스템 계정)
	 * @return 반납·스킵·실패 건수 결과
	 */
	public LockerExpirationResult releaseExpiredLockers(User actor) {
		List<Locker> expiredLockers = lockerReader.findExpiredLockers(LocalDateTime.now());

		int releasedCount = 0;
		int skippedCount = 0;
		int failedCount = 0;

		for (Locker locker : expiredLockers) {
			try {
				if (lockerExpirationProcessor.releaseExpiredLocker(locker.getId(), actor)) {
					releasedCount++;
				} else {
					skippedCount++;
				}
			} catch (Exception e) {
				failedCount++;
				log.error("[사물함 만료 반납] 처리 실패 - lockerId: {}, 원인: {}", locker.getId(), e.getMessage(), e);
			}
		}

		log.info("[사물함 만료 반납] 완료 - 대상 {}건, 반납 {}건, 스킵 {}건, 실패 {}건",
			expiredLockers.size(), releasedCount, skippedCount, failedCount);

		return new LockerExpirationResult(releasedCount, skippedCount, failedCount);
	}
}
