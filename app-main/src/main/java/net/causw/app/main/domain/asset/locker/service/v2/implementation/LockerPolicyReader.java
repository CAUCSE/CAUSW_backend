package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.etc.flag.service.v2.implementation.FlagReader;
import net.causw.app.main.domain.etc.textfield.service.v2.implementation.TextFieldReader;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerPolicyReader {

	private final FlagReader flagReader;
	private final TextFieldReader textFieldReader;

	public boolean isRegisterPeriod() {
		return flagReader.findValueByKey(StaticValue.LOCKER_ACCESS);
	}

	public boolean isReturnPeriod() {
		return flagReader.findValueByKey(StaticValue.LOCKER_ACCESS);
	}

	public boolean isExtendPeriod() {
		return flagReader.findValueByKey(StaticValue.LOCKER_EXTEND);
	}

	public LocalDateTime findExpireDate() {
		return LocalDateTime.parse(
			textFieldReader.findValueByKey(StaticValue.EXPIRED_AT)
				.orElseThrow(LockerErrorCode.LOCKER_EXPIRE_DATE_NOT_SET::toBaseException),
			StaticValue.LOCKER_DATE_TIME_FORMATTER);
	}

	public Optional<LocalDateTime> findExpireDateOptional() {
		return parseDateTime(StaticValue.EXPIRED_AT);
	}

	public LocalDateTime findNextExpireDate() {
		return LocalDateTime.parse(
			textFieldReader.findValueByKey(StaticValue.NEXT_EXPIRED_AT)
				.orElseThrow(LockerErrorCode.LOCKER_NEXT_EXPIRE_DATE_NOT_SET::toBaseException),
			StaticValue.LOCKER_DATE_TIME_FORMATTER);
	}

	public Optional<LocalDateTime> findNextExpireDateOptional() {
		return parseDateTime(StaticValue.NEXT_EXPIRED_AT);
	}

	public Optional<LocalDateTime> findRegisterStartDate() {
		return parseDateTime(StaticValue.REGISTER_START_AT);
	}

	public Optional<LocalDateTime> findRegisterEndDate() {
		return parseDateTime(StaticValue.REGISTER_END_AT);
	}

	public Optional<LocalDateTime> findExtendStartDate() {
		return parseDateTime(StaticValue.EXTEND_START_AT);
	}

	public Optional<LocalDateTime> findExtendEndDate() {
		return parseDateTime(StaticValue.EXTEND_END_AT);
	}

	private Optional<LocalDateTime> parseDateTime(String key) {
		return textFieldReader.findValueByKey(key)
			.map(value -> LocalDateTime.parse(value, StaticValue.LOCKER_DATE_TIME_FORMATTER));
	}
}
