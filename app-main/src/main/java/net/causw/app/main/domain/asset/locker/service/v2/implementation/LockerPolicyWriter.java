package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.etc.flag.entity.Flag;
import net.causw.app.main.domain.etc.flag.repository.FlagRepository;
import net.causw.app.main.domain.etc.textfield.entity.TextField;
import net.causw.app.main.domain.etc.textfield.repository.TextFieldRepository;
import net.causw.global.constant.StaticValue;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class LockerPolicyWriter {

	private final TextFieldRepository textFieldRepository;
	private final FlagRepository flagRepository;

	/**
	 * 사물함 신청 기간(시작/종료) 신청 시 만료일 설정
	 * @param start 신청 기간 시작일
	 * @param end 신청 기간 종료일
	 * @param expiredAt 신청시 만료일
	 */
	public void updateRegisterPeriod(LocalDateTime start, LocalDateTime end, LocalDateTime expiredAt) {
		setOrUpdateTextFieldDateTime(StaticValue.REGISTER_START_AT, start);
		setOrUpdateTextFieldDateTime(StaticValue.REGISTER_END_AT, end);
		setOrUpdateTextFieldDateTime(StaticValue.EXPIRED_AT, expiredAt);
	}

	/**
	 * 사물함 연장 기간(시작/종료) 연장 시 만료일 설정
	 * @param start 연장 기간 시작일
	 * @param end 연장 기간 종료일
	 * @param nextExpireDate 연장시 만료일
	 */
	public void updateExtendPeriod(LocalDateTime start, LocalDateTime end, LocalDateTime nextExpireDate) {
		setOrUpdateTextFieldDateTime(StaticValue.EXTEND_START_AT, start);
		setOrUpdateTextFieldDateTime(StaticValue.EXTEND_END_AT, end);
		setOrUpdateTextFieldDateTime(StaticValue.NEXT_EXPIRED_AT, nextExpireDate);
	}

	/**
	 * 사물함 신청가능 상태 설정
	 * @param status 신청 가능 상태
	 */
	public void updateRegisterStatus(@NotNull boolean status) {
		setOrUpdateFlagField(StaticValue.LOCKER_ACCESS, status);
	}

	/**
	 * 사물함 연장가능 상태 설정
	 * @param status 연장 가능 상태
	 */
	public void updateExtendStatus(@NotNull boolean status) {
		setOrUpdateFlagField(StaticValue.LOCKER_EXTEND, status);
	}

	/**
	 * 텍스트 필드 업데이트 (없으면 생성)
	 * @param key 키
	 * @param value 값
	 */
	private void setOrUpdateTextFieldDateTime(String key, LocalDateTime value) {
		textFieldRepository.findByKey(key)
			.ifPresentOrElse(
				textField -> textField.setValue(value.format(StaticValue.LOCKER_DATE_TIME_FORMATTER)),
				() -> textFieldRepository.save(TextField.of(key, value.format(StaticValue.LOCKER_DATE_TIME_FORMATTER))));
	}

	/**
	 * 플래그 필드 업데이트 (없으면 생성)
	 * @param key 키
	 * @param value 값
	 */
	private void setOrUpdateFlagField(String key, boolean value) {
		flagRepository.findByKey(key)
			.ifPresentOrElse(flag -> flag.setValue(value),
				() -> flagRepository.save(Flag.of(key, value)));
	}
}
