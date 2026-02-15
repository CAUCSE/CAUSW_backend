package net.causw.app.main.domain.asset.locker.service.v2;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerLogWriter;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPolicyReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerValidator;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockerService {

	private final LockerReader lockerReader;
	private final LockerPolicyReader lockerPolicyReader;
	private final LockerLogWriter lockerLogWriter;
	private final LockerValidator lockerValidator;

	/**
	 * 사물함 신청 (일반 유저용)
	 * 1. LOCKER_ACCESS 플래그 확인
	 * 2. 사물함 상태 검증 (비어있고, 활성화된 상태)
	 * 3. 기존 사물함 보유 시 자동 반납
	 * 4. 글로벌 만료일(EXPIRE_DATE) 기반으로 신청
	 *
	 * @param lockerId 신청할 사물함 ID
	 * @param user 신청 유저
	 */
	@Transactional
	public void registerLocker(String lockerId, User user) {
		lockerValidator.validateRegisterPeriod();

		Locker locker = lockerReader.findByIdForWrite(lockerId);
		lockerValidator.validateRegisterAvailable(locker);

		// 기존 사물함 보유 시 자동 반납
		lockerReader.findByUserId(user.getId()).ifPresent(existingLocker -> {
			existingLocker.returnLocker();
			lockerLogWriter.logReturn(existingLocker, user);
		});

		locker.register(user, lockerPolicyReader.findExpireDate());
		lockerLogWriter.logRegister(locker, user);
	}

	/**
	 * 사물함 반납 (일반 유저용)
	 * 1. LOCKER_ACCESS 플래그 확인
	 * 2. 사물함 사용중 상태 검증
	 * 3. 소유자 검증
	 * 4. 반납
	 *
	 * @param lockerId 반납할 사물함 ID
	 * @param user 반납 유저
	 */
	@Transactional
	public void returnLocker(String lockerId, User user) {
		lockerValidator.validateReturnPeriod();

		Locker locker = lockerReader.findByI / ㄷdForWrite(lockerId);
		lockerValidator.validateInUse(locker);
		lockerValidator.validateOwner(locker, user);

		locker.returnLocker();
		lockerLogWriter.logReturn(locker, user);
	}

	/**
	 * 사물함 연장 (일반 유저용)
	 * 1. LOCKER_EXTEND 플래그 확인
	 * 2. 사물함 사용중 상태 검증
	 * 3. 소유자 검증
	 * 4. 이미 연장 여부 검증
	 * 5. NEXT_EXPIRED_AT 기반으로 연장
	 *
	 * @param lockerId 연장할 사물함 ID
	 * @param user 연장 유저
	 */
	@Transactional
	public void extendLocker(String lockerId, User user) {
		lockerValidator.validateExtendPeriod();

		Locker locker = lockerReader.findByIdForWrite(lockerId);
		lockerValidator.validateInUse(locker);
		lockerValidator.validateOwner(locker, user);

		LocalDateTime nextExpireDate = lockerPolicyReader.findNextExpireDate();
		lockerValidator.validateNotAlreadyExtended(locker, nextExpireDate);

		locker.extendExpireDate(nextExpireDate);
		lockerLogWriter.logExtend(locker, user);
	}
}
