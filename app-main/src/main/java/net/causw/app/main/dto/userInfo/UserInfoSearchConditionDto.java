package net.causw.app.main.dto.userInfo;

import java.util.List;

import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

public record UserInfoSearchConditionDto(
	String keyword,
	Integer admissionYearStart,
	Integer admissionYearEnd,
	List<AcademicStatus> academicStatus
	) {
}
