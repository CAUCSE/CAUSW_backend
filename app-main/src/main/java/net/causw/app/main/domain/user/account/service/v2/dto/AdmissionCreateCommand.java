package net.causw.app.main.domain.user.account.service.v2.dto;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

import lombok.Builder;

@Builder
public record AdmissionCreateCommand(
	String description,
	AcademicStatus targetAcademicStatus,
	String studentId,
	Integer admissionYear,
	Department department) {
}
