package net.causw.application.dto.userCouncilFee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CurrentUserCouncilFeeResponseDto {

    @Schema(description = "학생회비 환불 여부", example = "false")
    private Boolean isRefunded;

    @Schema(description = "본 학기 학생회비 적용 여부", example = "true")
    private Boolean isAppliedThisSemester;

    @Schema(description = "납부한 학기 수", example = "8")
    private Integer numOfPaidSemester;

    @Schema(description = "잔여 학생회비 적용 학기", example = "3")
    private Integer restOfSemester;

}
