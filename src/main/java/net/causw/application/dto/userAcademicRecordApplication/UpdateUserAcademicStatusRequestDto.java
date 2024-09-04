package net.causw.application.dto.userAcademicRecordApplication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import net.causw.domain.model.enums.AcademicStatus;

@Getter
public class UpdateUserAcademicStatusRequestDto {

    @Schema(description = "대상 사용자 고유 id 값", requiredMode = Schema.RequiredMode.REQUIRED, example = "uuid 형식의 String 값입니다.")
    @Pattern(
            regexp = "^[0-9a-fA-F]{32}$",
            message = "대상 사용자 고유 id 값은 대시(-) 없이 32자리의 UUID 형식이어야 합니다."
    )
    @NotBlank(message = "대상 사용자 고유 id 값은 필수 입력 값입니다.")
    private String targetUserId;

    @Schema(description = "대상 사용자의 변경 타겟 학적 상태", defaultValue = "ENROLLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "재학/휴학/졸업/미정")
    private AcademicStatus targetAcademicStatus;

    @Schema(description = "대상 사용자의 현재 학기", defaultValue = "5", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "5")
    @Positive(message = "대상 사용자의 현재 학기는 1 이상의 자연수 값입니다.")
    private Integer targetCompletedSemester;

}
