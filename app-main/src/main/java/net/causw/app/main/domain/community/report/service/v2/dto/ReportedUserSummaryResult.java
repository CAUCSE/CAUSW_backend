package net.causw.app.main.domain.community.report.service.v2.dto;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record ReportedUserSummaryResult(
	String userId,
	String studentId,
	String name,
	AcademicStatus academicStatus,
	Integer reportedCount,
	UserState userState
) {
	public static ReportedUserSummaryResult from(User user) {
		return new ReportedUserSummaryResult(
			user.getId(),
			user.getStudentId(),
			user.getName(),
			user.getAcademicStatus(),
			user.getReportCount(),
			user.getState()
		);
	}
}
