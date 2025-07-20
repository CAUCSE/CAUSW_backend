package net.causw.app.main.dto.userAcademicRecordApplication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.model.enums.user.GraduationType;

@Getter
public class CreateUserAcademicRecordApplicationRequestDto {

    @Schema(description = "타겟 학적 상태", defaultValue = "ENROLLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "재학/휴학/졸업/미정")
    @NotNull(message = "타겟 학적 상태는 필수 입력 값입니다.")
    private AcademicStatus targetAcademicStatus;

    @Schema(description = "타겟 사용자의 현재 학기", defaultValue = "5", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "5")
    private Integer targetCompletedSemester;

    @Schema(description = "졸업 년도", defaultValue = "2026", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2026")
    private Integer graduationYear;

    @Schema(description = "졸업 유형", defaultValue = "FEBRUARY", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "FEBRUARY/AUGUST")
    private GraduationType graduationType;

    @Schema(description = "비고", defaultValue = "학적 정보 신청 비고", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "학적 정보 신청 비고입니다.")
    private String note;

}
