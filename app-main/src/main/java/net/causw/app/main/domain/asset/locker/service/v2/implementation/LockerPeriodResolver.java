package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.enums.LockerPeriodPhase;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.LockerPeriodStatusResult;

import lombok.RequiredArgsConstructor;

/**
 * 사물함 기간 판별 로직을 담당한다.
 *
 * <p>정책 값 조회는 {@link LockerPolicyReader}에 위임하고,
 * flag + 기간 기반의 비즈니스 판별만 수행한다.</p>
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerPeriodResolver {

	private final LockerPolicyReader lockerPolicyReader;

	/**
	 * 사물함이 특정 일시에 대해 신청 가능한 상태인지 확인
	 * @param targetTime 특정 일시
	 * @return 신청 가능 여부
	 */
	public boolean isRegisterActive(LocalDateTime targetTime) {
		boolean lockerAccess = lockerPolicyReader.getLockerAccessStatusFlag();
		boolean onRegisterPeriod = isOnRegisterPeriod(targetTime);

		return lockerAccess && onRegisterPeriod;
	}

	/**
	 * 사물함이 특정 일시에 대해 연장 가능한 상태인지 확인
	 * @param targetTime 특정 일시
	 * @return 연장 가능 여부
	 */
	public boolean isExtendActive(LocalDateTime targetTime) {
		boolean lockerExtend = lockerPolicyReader.getLockerExtendStatusFlag();
		boolean onExtendPeriod = isOnExtendPeriod(targetTime);

		return lockerExtend && onExtendPeriod;
	}

	/**
	 * 현재 시각 기준으로 사물함 기간 상태(phase)를 판별한다.
	 *
	 * <p>신청/연장 flag는 상호배타(동시 ON 불가)이므로,
	 * ON인 flag의 기간만 확인하여 READY/활성/CLOSED를 결정한다.</p>
	 *
	 * @param now 기준 시각
	 * @return 현재 phase와 해당 기간의 startAt/endAt
	 */
	public LockerPeriodStatusResult resolveCurrentPhase(LocalDateTime now) {
		if (lockerPolicyReader.getLockerAccessStatusFlag()) {
			return resolvePhase(now, LockerPeriodPhase.APPLY,
				lockerPolicyReader.findRegisterStartDate(),
				lockerPolicyReader.findRegisterEndDate());
		}

		if (lockerPolicyReader.getLockerExtendStatusFlag()) {
			return resolvePhase(now, LockerPeriodPhase.EXTEND,
				lockerPolicyReader.findExtendStartDate(),
				lockerPolicyReader.findExtendEndDate());
		}

		return closed();
	}

	/**
	 * 주어진 기간에 대해 now 위치를 판단하여 READY / activePhase / CLOSED를 반환한다.
	 */
	private LockerPeriodStatusResult resolvePhase(
		LocalDateTime now, LockerPeriodPhase activePhase,
		Optional<LocalDateTime> start, Optional<LocalDateTime> end) {

		if (start.isEmpty() || end.isEmpty()) {
			return closed();
		}

		if (now.isBefore(start.get())) {
			return LockerPeriodStatusResult.builder()
				.phase(LockerPeriodPhase.READY)
				.startAt(start.get())
				.endAt(end.get())
				.build();
		}

		if (!now.isAfter(end.get())) {
			return LockerPeriodStatusResult.builder()
				.phase(activePhase)
				.startAt(start.get())
				.endAt(end.get())
				.build();
		}

		return closed();
	}

	private LockerPeriodStatusResult closed() {
		return LockerPeriodStatusResult.builder()
			.phase(LockerPeriodPhase.CLOSED)
			.startAt(null)
			.endAt(null)
			.build();
	}

	/**
	 * 주어진 시각이 사물함 신청 가능 기간 내에 포함되는지 여부를 확인
	 * <br/> 신청 기간이 설정되지 않은 경우 false 반환
	 */
	private boolean isOnRegisterPeriod(LocalDateTime targetTime) {
		Optional<LocalDateTime> start = lockerPolicyReader.findRegisterStartDate();
		Optional<LocalDateTime> end = lockerPolicyReader.findRegisterEndDate();

		if (start.isEmpty() || end.isEmpty()) {
			//			throw LockerErrorCode.LOCKER_REGISTER_PERIOD_NOT_SET.toBaseException();
			return false;
		}

		return isOnPeriod(targetTime, start, end);
	}

	/**
	 * 주어진 시각이 사물함 연장 가능 기간 내에 포함되는지 여부를 확인
	 * <br/> 연장 기간이 설정되지 않은 경우 false 반환
	 */
	private boolean isOnExtendPeriod(LocalDateTime targetTime) {
		Optional<LocalDateTime> start = lockerPolicyReader.findExtendStartDate();
		Optional<LocalDateTime> end = lockerPolicyReader.findExtendEndDate();

		if (start.isEmpty() || end.isEmpty()) {
			return false;
		}

		return isOnPeriod(targetTime, start, end);
	}

	/**
	 * 주어진 시각이 [start, end] 범위 내에 포함되는지 확인한다.
	 * start 또는 end가 비어있으면 false를 반환한다.
	 */
	private boolean isOnPeriod(LocalDateTime targetTime,
		Optional<LocalDateTime> start, Optional<LocalDateTime> end) {
		if (start.isEmpty() || end.isEmpty()) {
			return false;
		}
		return !targetTime.isBefore(start.get()) && !targetTime.isAfter(end.get());
	}
}
