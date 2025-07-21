package net.causw.app.main.dto.userCouncilFee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CreateUserCouncilFeeWithUserRequestDto {

    @Schema(description = "user 고유 id값(서비스 가입 시만 존재)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "uuid 형식의 String 값입니다.")
    @NotBlank(message = "user 고유 id 값은 필수 입력 값입니다.")
    private String userId;

    @Schema(description = "납부 시점 학기", defaultValue = "1", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @Positive(message = "납부 시점 학기는 1 이상의 자연수 값입니다.")
    private Integer paidAt;

    @Schema(description = "납부한 학기 수", defaultValue = "8", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    @Positive(message = "납부한 학기 수는 1 이상의 자연수 값입니다.")
    private Integer numOfPaidSemester;

    @Schema(description = "환불 여부", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "환불 여부는 필수 입력 값입니다.")
    private Boolean isRefunded;

    @Schema(description = "환불 시점(isRefunded가 true일 때만 존재", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2021-01-01")
    private Integer refundedAt;

}
