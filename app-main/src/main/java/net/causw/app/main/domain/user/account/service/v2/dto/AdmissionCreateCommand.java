package net.causw.app.main.domain.user.account.service.v2.dto;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

public record AdmissionCreateCommand(
	String description,
	AcademicStatus requestedAcademicStatus,
	String requestedStudentId,
	Integer requestedAdmissionYear,
	Department requestedDepartment) {
}
