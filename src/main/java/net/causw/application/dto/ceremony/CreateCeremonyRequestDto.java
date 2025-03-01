package net.causw.application.dto.ceremony;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import net.causw.domain.model.enums.ceremony.CeremonyCategory;

import java.time.LocalDate;

@Getter
public class CreateCeremonyRequestDto {

    @Schema(description = "행사 설명", requiredMode = Schema.RequiredMode.REQUIRED, example = "연례 졸업식")
    @NotNull(message = "설명은 필수 입력 값입니다.")
    private String description;

    @Schema(description = "행사 시작 날짜", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-05-01")
    @NotNull(message = "시작 날짜는 필수 입력 값입니다.")
    private LocalDate startDate;

    @Schema(description = "행사 종료 날짜", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-05-02")
    @NotNull(message = "종료 날짜는 필수 입력 값입니다.")
    private LocalDate endDate;

    @Schema(description = "행사 카테고리", requiredMode = Schema.RequiredMode.REQUIRED, example = "MARRIAGE")
    @NotNull(message = "카테고리는 필수 입력 값입니다.")
    private CeremonyCategory category;
}
