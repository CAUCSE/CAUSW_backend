package net.causw.app.main.domain.community.report.service.v2.dto;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record ReportedUserListCondition(
	String keyword,
	UserState state,
	AcademicStatus academicStatus
) {
}
