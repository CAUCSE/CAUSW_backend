package net.causw.app.main.domain.asset.locker.service.v2;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLog;
import net.causw.app.main.domain.asset.locker.service.v2.dto.LockerListCondition;
import net.causw.app.main.domain.asset.locker.service.v2.dto.LockerLogListCondition;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerLogReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerLogWriter;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerValidator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockerAdminService {

	private final LockerReader lockerReader;
	private final LockerLogReader lockerLogReader;
	private final LockerLogWriter lockerLogWriter;
	private final LockerValidator lockerValidator;
	private final UserReader userReader;

	/**
	 * 사물함 로그 리스트 서비스 로직
	 * @param condition 사물함 로그 검색 조건 (유저 이름/이메일, 사물함 액션, 사물함 층수, 사물함 번호)
	 * @param pageable 페이지 요청
	 * @return 사물함 페이지
	 */
	@Transactional(readOnly = true)
	public Page<LockerLog> getLockerLogList(LockerLogListCondition condition, Pageable pageable) {
		return lockerLogReader.findLockerLogList(
			condition.userKeyword(),
			condition.action(),
			condition.lockerLocationName(),
			condition.lockerNumber(),
			pageable);
	}

	/**
	 * 사물함 리스트 서비스 로직
	 * @param condition 사물함 검색 조건 (유저 이름/이메일/학번, 사물함 층, 활성화 여부, 소유중 여부, 만료 여부)
	 * @param pageable 페이지 요청
	 * @return 사물함 페이지
	 */
	@Transactional(readOnly = true)
	public Page<Locker> getLockerList(LockerListCondition condition, Pageable pageable) {
		return lockerReader.findLockerList(
			condition.userKeyword(),
			condition.location(),
			condition.isActive(),
			condition.isOccupied(),
			condition.isExpired(),
			pageable);
	}

	/**
	 * 사물함 배정 (관리자용)
	 * - 신청 기간(LOCKER_ACCESS) 무시
	 * - 명시적 만료일 직접 지정
	 *
	 * @param lockerId 사물함 아이디
	 * @param userId 배정 예정 유저 아이디
	 * @param expiredAt 만료일
	 */
	@Transactional
	public void assignLocker(String lockerId, String userId, LocalDateTime expiredAt) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);

		lockerValidator.validateAssignable(locker);
		lockerValidator.validateUserNotHavingLocker(userId);

		User user = userReader.findUserById(userId);
		locker.register(user, expiredAt);
	}

	/**
	 * 사물함 연장
	 * @param lockerId 사물함 아이디
	 * @param expiredAt 연장예정 배정일
	 */
	@Transactional
	public void extendLocker(String lockerId, LocalDateTime expiredAt) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);

		lockerValidator.validateInUse(locker);

		locker.extendExpireDate(expiredAt);
	}

	/**
	 * 사물함 회수
	 * @param lockerId 사물함 아이디
	 */
	@Transactional
	public void releaseLocker(String lockerId) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);

		lockerValidator.validateInUse(locker);

		locker.returnLocker();
	}

	/**
	 * 사물함 활성화
	 * @param lockerId 사물함 아이디
	 */
	@Transactional
	public void enableLocker(String lockerId) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);

		lockerValidator.validateEnableable(locker);

		locker.activate();
		lockerLogWriter.logEnable(locker);
	}

	/**
	 * 사물함 비활성화
	 * @param lockerId 사물함 아이디
	 */
	@Transactional
	public void disableLocker(String lockerId) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);

		lockerValidator.validateDisableable(locker);

		locker.deactivate();
		lockerLogWriter.logDisable(locker);
	}
}
