package net.causw.app.main.domain.asset.locker.service.v2;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response.LockerPolicyResponse;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPeriodResolver;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPolicyReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPolicyWriter;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerValidator;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

/**
 * 사물함 정책 관리 서비스.
 *
 * <p>사물함 신청·연장 기간 설정 및 상태(활성/비활성) 전환을 담당한다.
 * 신청과 연장은 동시에 활성화할 수 없으며, 상태 전환 시 상호 배타 검증을 수행한다.</p>
 *
 * @see LockerService 일반 유저용 사물함 서비스
 * @see LockerAdminService 관리자용 사물함 서비스
 */
@Service
@RequiredArgsConstructor
public class LockerPolicyAdminService {

	private final LockerPolicyReader lockerPolicyReader;
	private final LockerPolicyWriter lockerPolicyWriter;
	private final LockerValidator lockerValidator;
	private final LockerPeriodResolver lockerPeriodResolver;

	/**
	 * 현재 사물함 정책 상태를 조회한다.
	 *
	 * @return 만료일, 신청/연장 기간, 신청/연장 활성 상태를 포함한 정책 정보
	 */
	@Transactional(readOnly = true)
	public LockerPolicyResponse getPolicy() {
		return new LockerPolicyResponse(
			lockerPolicyReader.findExpireDateOptional().orElse(null),
			lockerPolicyReader.findRegisterStartDate().orElse(null),
			lockerPolicyReader.findRegisterEndDate().orElse(null),
			lockerPolicyReader.findExtendStartDate().orElse(null),
			lockerPolicyReader.findExtendEndDate().orElse(null),
			lockerPolicyReader.findNextExpireDateOptional().orElse(null),
			lockerPolicyReader.getLockerAccessStatusFlag(),
			lockerPolicyReader.getLockerExtendStatusFlag());
	}

	/**
	 * 사물함 신청 기간을 설정한다.
	 * <p>신청 가능 상태가 활성화 중일 때는 수정할 수 없다.</p>
	 *
	 * @param start 신청 시작일시
	 * @param end 신청 종료일시
	 * @param expiredAt 신청된 사물함의 만료일시
	 */
	@Transactional
	public void updateRegisterPeriod(LocalDateTime start, LocalDateTime end, LocalDateTime expiredAt) {
		if (lockerPeriodResolver.isRegisterActive(LocalDateTime.now())) {
			throw LockerErrorCode.LOCKER_REGISTER_ACTIVE_CANNOT_UPDATE_PERIOD.toBaseException();
		}
		lockerValidator.validatePeriodOrder(start, end, expiredAt);
		lockerPolicyWriter.updateRegisterPeriod(start, end, expiredAt);
	}

	/**
	 * 사물함 연장 기간을 설정한다.
	 * <p>연장 가능 상태가 활성화 중일 때는 수정할 수 없다.</p>
	 *
	 * @param start 연장 시작일시
	 * @param end 연장 종료일시
	 * @param nextExpireDate 연장 후 새 만료일시
	 */
	@Transactional
	public void updateExtendPeriod(LocalDateTime start, LocalDateTime end, LocalDateTime nextExpireDate) {
		if (lockerPeriodResolver.isExtendActive(LocalDateTime.now())) {
			throw LockerErrorCode.LOCKER_EXTEND_ACTIVE_CANNOT_UPDATE_PERIOD.toBaseException();
		}
		lockerValidator.validatePeriodOrder(start, end, nextExpireDate);
		lockerPolicyWriter.updateExtendPeriod(start, end, nextExpireDate);
	}

	/**
	 * 사물함 신청 활성 상태를 변경한다.
	 *
	 * <p>연장이 활성화된 상태에서는 신청을 활성화할 수 없다.</p>
	 *
	 * @param status 활성화 여부
	 * @throws net.causw.app.main.shared.exception.BaseException 연장이 이미 활성 상태인 경우
	 */
	@Transactional
	public void updateRegisterStatus(@NotNull boolean status) {
		if (status && lockerPolicyReader.getLockerExtendStatusFlag()) {
			throw LockerErrorCode.LOCKER_EXTEND_ALREADY_ACTIVE.toBaseException();
		}
		lockerPolicyWriter.updateRegisterStatus(status);
	}

	/**
	 * 사물함 연장 활성 상태를 변경한다.
	 *
	 * <p>신청이 활성화된 상태에서는 연장을 활성화할 수 없다.</p>
	 *
	 * @param status 활성화 여부
	 * @throws net.causw.app.main.shared.exception.BaseException 신청이 이미 활성 상태인 경우
	 */
	@Transactional
	public void updateExtendStatus(@NotNull boolean status) {
		if (status && lockerPolicyReader.getLockerAccessStatusFlag()) {
			throw LockerErrorCode.LOCKER_REGISTER_ALREADY_ACTIVE.toBaseException();
		}
		lockerPolicyWriter.updateExtendStatus(status);
	}
}
