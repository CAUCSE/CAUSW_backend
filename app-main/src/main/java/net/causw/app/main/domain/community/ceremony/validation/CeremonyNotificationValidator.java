package net.causw.app.main.domain.community.ceremony.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

@Component
public class CeremonyNotificationValidator {

	public void validateNotificationTarget(boolean isSetAll, List<String> targetAdmissionYears) {
		if (!isSetAll) {
			if (targetAdmissionYears == null || targetAdmissionYears.isEmpty()) {
				throw CeremonyErrorCode.TARGET_ADMISSION_YEARS_REQUIRED.toBaseException();
			}
			for (String admissionYear : targetAdmissionYears) {
				if (!admissionYear.matches("^[0-9]{2}$")) {
					throw CeremonyErrorCode.INVALID_ADMISSION_YEARS_FORMAT.toBaseException();
				}
			}
		}
	}
}
