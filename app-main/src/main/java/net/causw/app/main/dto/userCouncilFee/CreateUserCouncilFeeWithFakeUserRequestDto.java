package net.causw.app.main.dto.userCouncilFee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.model.enums.user.GraduationType;


@Getter
public class CreateUserCouncilFeeWithFakeUserRequestDto {

    @Schema(description = "이름", requiredMode = Schema.RequiredMode.REQUIRED, example = "정상제")
    private String userName;

    @Schema(description = "학번", requiredMode = Schema.RequiredMode.REQUIRED, example = "20191234")
    private String studentId;

    @Schema(description = "입학년도", requiredMode = Schema.RequiredMode.REQUIRED, example = "2019")
    private Integer admissionYear;

    @Schema(description = "전공", requiredMode = Schema.RequiredMode.REQUIRED, example = "컴퓨터공학과")
    private String major;

    @Schema(description = "학적상태", requiredMode = Schema.RequiredMode.REQUIRED, example = "ENROLLED")
    private AcademicStatus academicStatus;

    @Schema(description = "등록 완료 학기", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer currentCompletedSemester;

    @Schema(description = "졸업년도", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023")
    private Integer graduationYear;

    @Schema(description = "졸업 유형", requiredMode = Schema.RequiredMode.REQUIRED, example = "FEBRUARY")
    private GraduationType graduationType;

    @Schema(description = "전화번호", requiredMode = Schema.RequiredMode.REQUIRED, example = "010-1234-5678")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식에 맞지 않습니다.")
    private String phoneNumber;

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
