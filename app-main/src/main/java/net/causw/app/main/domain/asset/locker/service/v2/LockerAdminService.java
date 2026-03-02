package net.causw.app.main.domain.asset.locker.service.v2;

import java.time.LocalDateTime;
import java.util.Optional;

import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
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
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

/**
 * 관리자용 사물함 서비스.
 *
 * <p>사물함 배정·회수·연장·활성화·비활성화 등 관리자 전용 기능을 제공한다.
 * 일반 유저의 신청/반납과 달리 정책 기간 제약 없이 동작하며,
 * 모든 변경에 대해 관리자 로그를 기록한다.</p>
 *
 * @see LockerService 일반 유저용 사물함 서비스
 * @see LockerPolicyAdminService 사물함 정책 관리 서비스
 */
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
	public void assignLocker(String lockerId, String userId, LocalDateTime expiredAt, String adminId) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);
		User admin = userReader.findAdminUserById(adminId);

		lockerValidator.validateAssignable(locker);
		lockerValidator.validateUserNotHavingLocker(userId);

		User user = userReader.findUserByIdNotDeleted(userId);
		locker.register(user, expiredAt);

		lockerLogWriter.logAdminAssign(locker, admin, user);
	}

	/**
	 * 사물함 연장
	 * @param lockerId 사물함 아이디
	 * @param expiredAt 연장예정 배정일
	 * @param adminId 관리자 아이디
	 */
	@Transactional
	public void extendLocker(String lockerId, LocalDateTime expiredAt, String adminId) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);
		User admin = userReader.findAdminUserById(adminId);

		lockerValidator.validateInUse(locker);

		User user = locker.getUser().orElseThrow(UserErrorCode.USER_NOT_FOUND::toBaseException);

		locker.extendExpireDate(expiredAt);
		lockerLogWriter.logAdminExtend(locker, admin, user);
	}

	/**
	 * 사물함 회수
	 * @param lockerId 사물함 아이디
	 * @param adminId 관리자 아이디
	 */
	@Transactional
	public void releaseLocker(String lockerId, String adminId) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);
		User admin = userReader.findAdminUserById(adminId);

		lockerValidator.validateInUse(locker);

		User user = locker.getUser().orElseThrow(UserErrorCode.USER_NOT_FOUND::toBaseException);

		locker.returnLocker();
		lockerLogWriter.logAdminRelease(locker, admin, user.getEmail(), user.getName());
	}

	/**
	 * 사물함 활성화
	 * @param lockerId 사물함 아이디
	 * @param adminId 관리자 아이디
	 */
	@Transactional
	public void enableLocker(String lockerId, String adminId) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);
		User admin = userReader.findAdminUserById(adminId);

		lockerValidator.validateEnableable(locker);

		locker.enable();
		lockerLogWriter.logEnable(locker, admin);
	}

	/**
	 * 사물함 비활성화
	 * @param lockerId 사물함 아이디
	 * @param adminId 관리자 아이디
	 */
	@Transactional
	public void disableLocker(String lockerId, String adminId) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);
		User admin = userReader.findAdminUserById(adminId);

		lockerValidator.validateDisableable(locker);

		// locker user 존재할 시에 반환
		var user = locker.getUser();
		if (user.isPresent()) {
			locker.returnLocker();
			lockerLogWriter.logAdminRelease(locker, admin, user.get().getEmail(), user.get().getName());
		}

		locker.disable();
		lockerLogWriter.logDisable(locker, admin);
	}

	public void releaseExpiredLocker(String adminId) {
		User admin = userReader.findAdminUserById(adminId);

		var expiredLockers = lockerReader.findExpiredLockers(LocalDateTime.now());
		expiredLockers.forEach(locker -> {
			locker.returnLocker();
			var userEmail = locker.getUser().map(User::getEmail).orElse("알 수 없음");
			var userName = locker.getUser().map(User::getName).orElse("알 수 없음");
			lockerLogWriter.logAdminRelease(locker, admin, userEmail, userName);
		});
	}
}
