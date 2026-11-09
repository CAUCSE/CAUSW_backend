package net.causw.app.main.domain.admin.emailcampaign.repository.query;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

public record EmailCampaignTarget(
	String userId,
	String email,
	Integer admissionYear,
	Department department,
	AcademicStatus academicStatus) {
}
