package net.causw.app.main.dto.userAcademicRecordApplication;

import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UpdateUserAcademicRecordApplicationRequestDto {

	@Schema(description = "변경 목표 학적 정보 신청 고유 id 값", requiredMode = Schema.RequiredMode.REQUIRED, example = "uuid 형식의 String 값입니다.")
	@Pattern(
		regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
		message = "id 값은 대시(-)를 포함하고, 32자리의 UUID 형식이어야 합니다."
	)
	@NotBlank(message = "변경 목표 학적 정보 신청 고유 id 값은 필수 입력 값입니다.")
	private String applicationId;

	@Schema(description = "타겟 학적 상태", defaultValue = "ENROLLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "재학/휴학/졸업/미정")
	private AcademicStatus targetAcademicStatus;

	@Schema(description = "타겟 사용자의 현재 학기", defaultValue = "5", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
	private Integer targetCompletedSemester;

	@Schema(description = "비고", defaultValue = "학적 정보 신청 비고", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "학적 정보 신청 비고입니다.")
	private String note;

}
