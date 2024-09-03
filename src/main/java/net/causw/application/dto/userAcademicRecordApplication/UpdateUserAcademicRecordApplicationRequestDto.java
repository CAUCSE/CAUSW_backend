package net.causw.application.dto.userAcademicRecordApplication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import net.causw.domain.model.enums.AcademicRecordRequestStatus;

@Getter
public class UpdateUserAcademicRecordApplicationRequestDto {

    @Schema(description = "변경 목표 학적 정보 인증 상태", defaultValue = "ACCEPT", requiredMode = Schema.RequiredMode.REQUIRED, example = "변경하고자 하는 학적 정보 인증 상태 입니다. (ACCEPT, REJECT, AWAIT)")
    @NotBlank(message = "변경 목표 학적 정보 인증 상태는 필수 입력 값입니다.")
    private AcademicRecordRequestStatus targetAcademicRecordRequestStatus;

    @Schema(description = "거절 사유", defaultValue = "학적 정보 인증 거절 사유", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "학적 정보 인증 거절 사유입니다.")
    private String reason;

}
