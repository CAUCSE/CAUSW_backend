package net.causw.app.main.domain.community.ceremony.util;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

@Component
public class CeremonyNotificationValidator {

	public void validateNotificationTarget(boolean isSetAll, List<Integer> targetAdmissionYears) {
		if (!isSetAll) {
			if (targetAdmissionYears == null || targetAdmissionYears.isEmpty()) {
				throw CeremonyErrorCode.TARGET_ADMISSION_YEARS_REQUIRED.toBaseException();
			}
		}
	}
}
