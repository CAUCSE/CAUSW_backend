package net.causw.app.main.domain.user.academic.api.v2.dto.response;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학적 상태 변경 응답")
public record AcademicStatusResponse<T>(
	@Schema(description = "요청한 학적 상태", example = "GRADUATED") AcademicStatus requestedStatus,
	@Schema(description = "변경 후 현재 학적 상태", example = "GRADUATED") AcademicStatus updatedStatus,
	@Schema(description = "학적 상태 변경 상세 정보", oneOf = {
		GraduationDetailsResponse.class, EnrollmentDetailsResponse.class}) T recordDetails){
}
