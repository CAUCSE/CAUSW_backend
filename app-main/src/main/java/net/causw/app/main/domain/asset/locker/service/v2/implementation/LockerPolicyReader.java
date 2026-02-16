package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import java.time.LocalDateTime;
import java.util.Optional;

import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.etc.flag.service.v2.implementation.FlagReader;
import net.causw.app.main.domain.etc.textfield.service.v2.implementation.TextFieldReader;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerPolicyReader {

	private final FlagReader flagReader;
	private final TextFieldReader textFieldReader;

	/**
	 * 사물함이 특정 일시에 대해 신청 가능한 상태인지 확인
	 * @param targetTime 특정 일시
	 * @return 신청 가능 여부
	 */
	public boolean isRegisterActive(LocalDateTime targetTime) {
		boolean lockerAccess = flagReader.findValueByKey(StaticValue.LOCKER_ACCESS);
		boolean onRegisterPeriod = isOnRegisterPeriod(targetTime);

		return lockerAccess && onRegisterPeriod;
	}

	/**
	 * 사물함이 특정 일시에 대해 연장 가능한 상태인지 확인
	 * @param targetTime 특정 일시
	 * @return 연장 가능 여부
	 */
	public boolean isExtendActive(LocalDateTime targetTime) {
		boolean lockerExtend = flagReader.findValueByKey(StaticValue.LOCKER_EXTEND);
		boolean onExtendPeriod = isOnExtendPeriod(targetTime);

		return lockerExtend && onExtendPeriod;
	}

	/**
	 * 사물함 신청 가능 상태 flag 반환
	 * @return 사물함 신청 상태 flag
	 */
	public boolean getLockerAccessStatusFlag() {
		return flagReader.findValueByKey(StaticValue.LOCKER_ACCESS);
	}

	/**
	 * 사물함 연장 가능 상태 flag 반환
	 * @return 사물함 연장 가능 상태 flag
	 */
	public boolean getLockerExtendStatusFlag() {
		return flagReader.findValueByKey(StaticValue.LOCKER_EXTEND);
	}

	/**
	 * 현재 설정된 사물함 만료 일시를 반환
	 * @return 설정된 만료 일시
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 만료 일시가 설정되지 않은 경우
	 */
	public LocalDateTime findExpireDate() {
		return LocalDateTime.parse(
			textFieldReader.findValueByKey(StaticValue.EXPIRED_AT)
				.orElseThrow(LockerErrorCode.LOCKER_EXPIRE_DATE_NOT_SET::toBaseException),
			StaticValue.LOCKER_DATE_TIME_FORMATTER);
	}

	/**
	 * 현재 설정된 사물함 만료 일시를 Optional 형태로 반환
	 * @return 설정된 만료 일시(Optional), 미설정 시 empty
	 */
	public Optional<LocalDateTime> findExpireDateOptional() {
		return parseDateTime(StaticValue.EXPIRED_AT);
	}

	/**
	 * 다음 회차 사물함 만료 일시를 반환
	 * @return 다음 회차 만료 일시
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 다음 회차 만료 일시가 설정되지 않은 경우
	 */
	public LocalDateTime findNextExpireDate() {
		return LocalDateTime.parse(
			textFieldReader.findValueByKey(StaticValue.NEXT_EXPIRED_AT)
				.orElseThrow(LockerErrorCode.LOCKER_NEXT_EXPIRE_DATE_NOT_SET::toBaseException),
			StaticValue.LOCKER_DATE_TIME_FORMATTER);
	}

	/**
	 * 다음 회차 사물함 만료 일시를 Optional 형태로 반환
	 * @return 다음 회차 만료 일시(Optional), 미설정 시 empty
	 */
	public Optional<LocalDateTime> findNextExpireDateOptional() {
		return parseDateTime(StaticValue.NEXT_EXPIRED_AT);
	}

	/**
	 * 사물함 신청 시작 일시를 Optional 형태로 반환
	 * @return 신청 시작 일시(Optional), 미설정 시 empty
	 */
	public Optional<LocalDateTime> findRegisterStartDate() {
		return parseDateTime(StaticValue.REGISTER_START_AT);
	}

	/**
	 * 사물함 신청 종료 일시를 Optional 형태로 반환
	 * @return 신청 종료 일시(Optional), 미설정 시 empty
	 */
	public Optional<LocalDateTime> findRegisterEndDate() {
		return parseDateTime(StaticValue.REGISTER_END_AT);
	}

	/**
	 * 사물함 연장 시작 일시를 Optional 형태로 반환
	 * @return 연장 시작 일시(Optional), 미설정 시 empty
	 */
	public Optional<LocalDateTime> findExtendStartDate() {
		return parseDateTime(StaticValue.EXTEND_START_AT);
	}

	/**
	 * 사물함 연장 종료 일시를 Optional 형태로 반환
	 * @return 연장 종료 일시(Optional), 미설정 시 empty
	 */
	public Optional<LocalDateTime> findExtendEndDate() {
		return parseDateTime(StaticValue.EXTEND_END_AT);
	}

	/**
	 * 텍스트 필드에 저장된 문자열 값을 LocalDateTime으로 파싱
	 * @param key 텍스트 필드 키
	 * @return 파싱된 LocalDateTime(Optional), 값이 없으면 empty
	 */
	private Optional<LocalDateTime> parseDateTime(String key) {
		return textFieldReader.findValueByKey(key)
			.map(value -> LocalDateTime.parse(value, StaticValue.LOCKER_DATE_TIME_FORMATTER));
	}

	/**
	 * 주어진 시각이 사물함 신청 가능 기간 내에 포함되는지 여부를 확인
	 * @param targetTime 확인할 시각
	 * @return 신청 가능 기간 내라면 true, 아니면 false
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 신청 기간이 설정되지 않은 경우
	 */
	private boolean isOnRegisterPeriod(LocalDateTime targetTime) {
		LocalDateTime registerStartAt = parseDateTime(StaticValue.REGISTER_START_AT)
				.orElseThrow(LockerErrorCode.LOCKER_REGISTER_PERIOD_NOT_SET::toBaseException);
		LocalDateTime registerEndAt = parseDateTime(StaticValue.REGISTER_END_AT)
				.orElseThrow(LockerErrorCode.LOCKER_REGISTER_PERIOD_NOT_SET::toBaseException);

		return (targetTime.equals(registerStartAt) || targetTime.isAfter(registerStartAt))
				&& (targetTime.equals(registerEndAt)   || targetTime.isBefore(registerEndAt));
	}

	/**
	 * 주어진 시각이 사물함 연장 가능 기간 내에 포함되는지 여부를 확인
	 * @param targetTime 확인할 시각
	 * @return 연장 가능 기간 내라면 true, 아니면 false
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 연장 기간이 설정되지 않은 경우
	 */
 	private boolean isOnExtendPeriod(LocalDateTime targetTime) {
		LocalDateTime extendStartAt = parseDateTime(StaticValue.EXTEND_START_AT)
				.orElseThrow(LockerErrorCode.LOCKER_EXTEND_PERIOD_NOT_SET::toBaseException);
		LocalDateTime extendEndAt = parseDateTime(StaticValue.EXTEND_START_AT)
				.orElseThrow(LockerErrorCode.LOCKER_EXTEND_PERIOD_NOT_SET::toBaseException);

		return (targetTime.equals(extendStartAt) || targetTime.isAfter(extendStartAt))
				&& (targetTime.equals(extendEndAt)   || targetTime.isBefore(extendEndAt));
	}
}
