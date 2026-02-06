package net.causw.app.main.domain.community.ceremony.validation;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

@Component
public class CeremonyDateTimeValidator {

	// 날짜, 시간 입력 값 검증 (startDate 입력은 Dto에서 NotNull로 검증)
	public void validateDateTime(LocalDate endDate, LocalTime startTime, LocalTime endTime) {
		boolean hasStartTime = startTime != null;
		boolean hasEndTime = endTime != null;

		if ((hasStartTime || hasEndTime) && endDate == null) {
			throw CeremonyErrorCode.END_DATE_REQUIRED.toBaseException();
		}

		if (hasStartTime && !hasEndTime) {
			throw CeremonyErrorCode.END_TIME_REQUIRED.toBaseException();
		}

		if (hasEndTime && !hasStartTime) {
			throw CeremonyErrorCode.START_TIME_REQUIRED.toBaseException();
		}
	}

	// 시작 일시가 종료 일시 이전이 아닌지 검증
	public void validateDateTimeRange(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
		if (endDate == null) {
			return;
		}

		if (endDate.isBefore(startDate)) {
			throw CeremonyErrorCode.DATETIME_END_AFTER_START.toBaseException();
		}

		if (startTime == null || endTime == null) {
			return;
		}

		if (startDate.isEqual(endDate) && endTime.isBefore(startTime)) {
			throw CeremonyErrorCode.DATETIME_END_AFTER_START.toBaseException();
		}
	}
}