package net.causw.app.main.domain.user.account.api.v2.dto.request;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "재학정보 인증 신청 요청 V2")
public record AdmissionCreateRequest(

	@Schema(description = "이름", example = "홍길동") @NotBlank(message = "이름을 입력해 주세요.") @Size(max = 20, message = "이름은 20자 이하여야 합니다.") String name,

	@Schema(description = "학과", example = "SCHOOL_OF_SW") @NotNull(message = "학과를 선택해 주세요.") Department requestedDepartment,

	@Schema(description = "입학년도", example = "2021") @NotNull(message = "입학년도를 입력해 주세요.") @Min(value = 1950, message = "올바른 네 자리 연도를 입력해 주세요.") @Max(value = 2200, message = "올바른 네 자리 연도를 입력해 주세요.") Integer requestedAdmissionYear,

	@Schema(description = "학번 (졸업자는 선택)", example = "20210001") @Pattern(regexp = "^$|^\\d{8}$|^\\d{10}$", message = "학번은 8자리 또는 10자리 숫자여야 합니다.") String requestedStudentId,

	@Schema(description = "재학분류", example = "ENROLLED") @NotNull(message = "재학 분류를 선택해 주세요.") AcademicStatus requestedAcademicStatus,

	@Schema(description = "졸업연도", example = "2025") @Min(value = 1950, message = "올바른 네 자리 연도를 입력해 주세요.") @Max(value = 2200, message = "올바른 네 자리 연도를 입력해 주세요.") Integer graduationYear,

	@Schema(description = "증빙설명", example = "재학증명서입니다.") @Size(max = 500, message = "증빙설명은 500자 이하여야 합니다.") String description) {
}
