package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerValidator {

	private final LockerPolicyReader lockerPolicyReader;
	private final LockerReader lockerReader;

	// ==================== User용 검증 ====================

	/**
	 * 사물함 신청 기간 검증
	 */
	public void validateRegisterPeriod(LocalDateTime time) {
		if (!lockerPolicyReader.isRegisterActive(time)) {
			throw LockerErrorCode.LOCKER_REGISTER_NOT_ALLOWED.toBaseException();
		}
	}

	/**
	 * 사물함 반납 기간 검증
	 */
	public void validateReturnPeriod(LocalDateTime time) {
		if (!lockerPolicyReader.isRegisterActive(time)) {
			throw LockerErrorCode.LOCKER_RETURN_NOT_ALLOWED.toBaseException();
		}
	}

	/**
	 * 사물함 연장 기간 검증
	 */
	public void validateExtendPeriod(LocalDateTime time) {
		if (!lockerPolicyReader.isExtendActive(time)) {
			throw LockerErrorCode.LOCKER_EXTEND_NOT_ALLOWED.toBaseException();
		}
	}

	/**
	 * 사물함 신청 가능 상태 검증 (유저용)
	 * 사물함이 비어있고(AVAILABLE) 활성화된 상태인지 확인
	 */
	public void validateRegisterAvailable(Locker locker) {
		LockerStatus status = LockerStatus.of(locker);
		if (status == LockerStatus.IN_USE) {
			throw LockerErrorCode.LOCKER_IN_USE.toBaseException();
		}
		if (status == LockerStatus.DISABLED) {
			throw LockerErrorCode.LOCKER_DISABLED.toBaseException();
		}
	}

	/**
	 * 사물함 소유자 검증
	 * 요청 유저가 해당 사물함의 소유자인지 확인
	 */
	public void validateOwner(Locker locker, User user) {
		locker.getUser()
			.filter(owner -> owner.getId().equals(user.getId()))
			.orElseThrow(LockerErrorCode.LOCKER_NOT_OWNER::toBaseException);
	}

	/**
	 * 사물함 이미 연장 여부 검증
	 * 현재 만료일이 연장 목표 만료일과 동일하면 이미 연장된 것
	 */
	public void validateNotAlreadyExtended(Locker locker, LocalDateTime nextExpireDate) {
		if (locker.getExpireDate() != null && locker.getExpireDate().isEqual(nextExpireDate)) {
			throw LockerErrorCode.LOCKER_ALREADY_EXTENDED.toBaseException();
		}
	}

	// ==================== Admin용 검증 ====================

	/**
	 * 사물함 배정 가능 상태 검증 (관리자용)
	 */
	public void validateAssignable(Locker locker) {
		if (LockerStatus.of(locker) != LockerStatus.AVAILABLE) {
			throw LockerErrorCode.LOCKER_NOT_AVAILABLE.toBaseException();
		}
	}

	/**
	 * 유저 사물함 중복 보유 검증 (관리자용)
	 */
	public void validateUserNotHavingLocker(String userId) {
		if (lockerReader.existsByUserId(userId)) {
			throw LockerErrorCode.LOCKER_USER_ALREADY_HAS_LOCKER.toBaseException();
		}
	}

	/**
	 * 사물함 사용중 상태 검증 (연장, 회수, 반납)
	 */
	public void validateInUse(Locker locker) {
		if (LockerStatus.of(locker) != LockerStatus.IN_USE) {
			throw LockerErrorCode.LOCKER_NOT_IN_USE.toBaseException();
		}
	}

	/**
	 * 사물함 활성화 가능 검증 (관리자용)
	 * 이미 활성 상태가 아닌지 확인
	 */
	public void validateEnableable(Locker locker) {
		if (Boolean.TRUE.equals(locker.getIsActive())) {
			throw LockerErrorCode.LOCKER_ALREADY_ACTIVE.toBaseException();
		}
	}

	/**
	 * 사물함 비활성화 가능 검증 (관리자용)
	 * 이미 비활성 상태가 아닌지 확인
	 */
	public void validateDisableable(Locker locker) {
		if (!Boolean.TRUE.equals(locker.getIsActive())) {
			throw LockerErrorCode.LOCKER_ALREADY_DISABLED.toBaseException();
		}
	}
}
