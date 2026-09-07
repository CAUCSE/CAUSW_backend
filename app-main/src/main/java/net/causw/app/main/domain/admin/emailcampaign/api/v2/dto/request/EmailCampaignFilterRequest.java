package net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request;

import java.util.List;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmailCampaignFilterRequest(
	@Schema(description = "입학연도 목록") List<Integer> admissionYears,
	@Schema(description = "학과 목록") List<Department> departments,
	@Schema(description = "학적 상태 목록") List<AcademicStatus> academicStatuses) {
}
