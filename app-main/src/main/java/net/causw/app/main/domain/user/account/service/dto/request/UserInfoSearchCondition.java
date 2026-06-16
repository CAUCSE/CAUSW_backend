package net.causw.app.main.domain.user.account.service.dto.request;

import java.util.List;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;

public record UserInfoSearchCondition(
	String keyword,
	Integer admissionYearStart,
	Integer admissionYearEnd,
	List<AcademicStatus> academicStatus
) {
}
