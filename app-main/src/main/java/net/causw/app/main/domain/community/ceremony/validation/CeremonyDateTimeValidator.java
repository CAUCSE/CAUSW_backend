package net.causw.app.main.domain.community.ceremony.validation;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

@Component
public class CeremonyDateTimeValidator {

	public void validateEndTime(LocalDate endDate, LocalTime endTime, LocalTime startTime) {
		if (endTime != null) {
			if (endDate == null) {
				throw CeremonyErrorCode.END_DATE_REQUIRED.toBaseException();
			}
			if (startTime == null) {
				throw CeremonyErrorCode.START_TIME_REQUIRED.toBaseException();
			}
		}
	}
}
