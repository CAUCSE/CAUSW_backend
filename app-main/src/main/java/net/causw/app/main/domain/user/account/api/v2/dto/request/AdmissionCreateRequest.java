package net.causw.app.main.domain.user.account.api.v2.dto.request;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "재학정보 인증 신청 요청 V2")
public record AdmissionCreateRequest(

	@Schema(description = "이름 (본명)", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "이름을 입력해 주세요.") String name,

	@Schema(description = "학과(부)", example = "SCHOOL_OF_SW", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "학과를 선택해 주세요.") Department requestedDepartment,

	@Schema(description = "입학년도", example = "2021", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "입학년도를 입력해 주세요.") @Min(value = 1950, message = "올바른 네 자리 입학년도를 입력해 주세요.") @Max(value = 2200, message = "올바른 네 자리 입학년도를 입력해 주세요.") Integer requestedAdmissionYear,

	@Schema(description = "학번", example = "20210001", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "학번을 입력해 주세요.") String requestedStudentId,

	@Schema(description = "재학 분류 (ENROLLED: 재적(휴학 포함), GRADUATED: 졸업)", example = "ENROLLED", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "재학 분류를 선택해 주세요.") AcademicStatus requestedAcademicStatus,

	@Schema(description = "졸업연도 (재학 분류가 GRADUATED인 경우 필수)", example = "2025") @Min(value = 1950, message = "올바른 네 자리 졸업연도를 입력해 주세요.") @Max(value = 2200, message = "올바른 네 자리 졸업연도를 입력해 주세요.") Integer graduationYear,

	@Schema(description = "증빙서류 설명", example = "재학증명서입니다.") String description) {
}
