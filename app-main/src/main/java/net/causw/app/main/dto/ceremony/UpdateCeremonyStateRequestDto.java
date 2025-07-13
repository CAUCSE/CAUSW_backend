package net.causw.app.main.dto.ceremony;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import net.causw.app.main.domain.model.enums.ceremony.CeremonyState;

@Getter
public class UpdateCeremonyStateRequestDto {

    @Schema(description = "대상 경조사 고유 ID 값", requiredMode = Schema.RequiredMode.REQUIRED, example = "uuid 형식의 String 값입니다.")
    @NotBlank(message = "대상 경조사 고유 ID 값은 필수 입력 값입니다.")
    private String ceremonyId;

    @Schema(description = "변경 목표 경조사 상태", defaultValue = "ACCEPT", requiredMode = Schema.RequiredMode.REQUIRED, example = "변경하고자 하는 경조사 상태입니다. (ACCEPT, REJECT, AWAIT, CLOSE)")
    @NotNull(message = "변경 목표 경조사 상태는 필수 입력 값입니다.")
    private CeremonyState targetCeremonyState;

    @Schema(description = "거절 사유", defaultValue = "경조사 거절 사유", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "경조사 거절 사유입니다. 거절시에만 입력")
    private String rejectMessage;
}
