package net.causw.application.dto.userAcademicRecordApplication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import net.causw.domain.model.enums.userAcademicRecord.AcademicRecordRequestStatus;

@Getter
public class UpdateUserAcademicRecordApplicationStateRequestDto {

    @Schema(description = "대상 사용자 고유 id 값", requiredMode = Schema.RequiredMode.REQUIRED, example = "uuid 형식의 String 값입니다.")
    @NotBlank(message = "대상 사용자 고유 id 값은 필수 입력 값입니다.")
    private String targetUserId;

    @Schema(description = "변경 목표 학적 정보 신청 고유 id 값", requiredMode = Schema.RequiredMode.REQUIRED, example = "uuid 형식의 String 값입니다.")
    @NotBlank(message = "변경 목표 학적 정보 신청 고유 id 값은 필수 입력 값입니다.")
    private String applicationId;

    @Schema(description = "변경 목표 학적 정보 인증 상태", defaultValue = "ACCEPT", requiredMode = Schema.RequiredMode.REQUIRED, example = "변경하고자 하는 학적 정보 인증 상태 입니다. (ACCEPT, REJECT, AWAIT)")
    @NotNull(message = "변경 목표 학적 정보 인증 상태는 필수 입력 값입니다.")
    private AcademicRecordRequestStatus targetAcademicRecordRequestStatus;

    @Schema(description = "거절 사유", defaultValue = "학적 정보 인증 거절 사유", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "학적 정보 인증 거절 사유입니다.")
    private String rejectMessage;

}
