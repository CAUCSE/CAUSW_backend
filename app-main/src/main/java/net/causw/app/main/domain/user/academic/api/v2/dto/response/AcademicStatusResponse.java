package net.causw.app.main.domain.user.academic.api.v2.dto.response;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;

public record AcademicStatusResponse<T>(
	AcademicStatus requestedStatus,
	AcademicStatus updatedStatus,
	T recordDetails) {
}
