package net.causw.app.main.domain.user.academic.util;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.shared.exception.errorcode.AcademicRecordApplicationErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AcademicRecordApplicationValidator {

	public static void validateAwaitStatus(UserAcademicRecordApplication application) {
		if (application.getAcademicRecordRequestStatus() != AcademicRecordRequestStatus.AWAIT) {
			throw AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_APPLICATION_NOT_AWAITING.toBaseException();
		}
	}
}
