package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.etc.textfield.entity.TextField;
import net.causw.app.main.domain.etc.textfield.repository.TextFieldRepository;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class LockerPolicyWriter {

	private final TextFieldRepository textFieldRepository;

	public void updateExpireDate(LocalDateTime expiredAt) {
		setOrUpdateTextField(StaticValue.EXPIRED_AT, expiredAt.toString());
	}

	public void updateRegisterPeriod(LocalDateTime start, LocalDateTime end) {
		setOrUpdateTextField(StaticValue.REGISTER_START_AT, start.toString());
		setOrUpdateTextField(StaticValue.REGISTER_END_AT, end.toString());
	}

	public void updateExtendPeriod(LocalDateTime start, LocalDateTime end, LocalDateTime nextExpireDate) {
		setOrUpdateTextField(StaticValue.EXTEND_START_AT, start.toString());
		setOrUpdateTextField(StaticValue.EXTEND_END_AT, end.toString());
		setOrUpdateTextField(StaticValue.NEXT_EXPIRED_AT, nextExpireDate.toString());
	}

	private void setOrUpdateTextField(String key, String value) {
		textFieldRepository.findByKey(key)
			.ifPresentOrElse(
				textField -> textField.setValue(value),
				() -> textFieldRepository.save(TextField.of(key, value)));
	}
}
