package net.causw.application.dto.userCouncilFee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.GraduationType;


@Getter
public class CreateUserCouncilFeeRequestDto {

    @Schema(description = "동문네트워크 서비스 가입 여부", defaultValue = "true", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "동문네트워크 서비스 가입 여부는 필수 입력 값입니다.")
    private Boolean isJoinedService;

    @Schema(description = "user 고유 id값(서비스 가입 시만 존재)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "uuid 형식의 String 값입니다.")
    @Pattern(
            regexp = "^[0-9a-fA-F]{32}$",
            message = "대상 사용자 고유 id 값은 대시(-) 없이 32자리의 UUID 형식이어야 합니다."
    )
    private String userId;

    @Schema(description = "이름", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "정상제")
    private String userName;

    @Schema(description = "학번", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "20191234")
    private String studentId;

    @Schema(description = "입학년도", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2019")
    private Integer admissionYear;

    @Schema(description = "전공", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "컴퓨터공학과")
    private String major;

    @Schema(description = "학적상태", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "재학")
    private AcademicStatus academicStatus;

    @Schema(description = "등록 완료 학기", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "5")
    private Integer currentCompletedSemester;

    @Schema(description = "졸업년도", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2023")
    private Integer graduationYear;

    @Schema(description = "졸업 유형", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "졸업예정")
    private GraduationType graduationType;

    @Schema(description = "전화번호", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "010-1234-5678")
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

    @AssertTrue(message = "isJoinedService가 true일 때 userId가 존재해야 합니다.")
    public boolean isValidUserOrFakeUser() {
        return (this.userId == null) != this.isJoinedService;
    }

    @AssertTrue(message = "userId가 존재하지 않으면 Fake User 정보 값은 유효한 값이 존재해야 하며, userId가 있으면 Fake User 정보 값들은 null이어야 합니다.")
    public boolean isValidFakeUserInfo() {
        // userId가 없으면 다른 값들이 모두 적절하게 설정되어 있어야 함
        if (this.userId == null) {
            boolean isUserNameValid = this.userName != null;
            boolean isStudentIdValid = this.studentId != null;
            boolean isAdmissionYearValid = this.admissionYear != null && this.admissionYear > 0;
            boolean isMajorValid = this.major != null;
            boolean isAcademicStatusValid = this.academicStatus != null;
            if (this.academicStatus != null) {
                if (this.academicStatus.equals(AcademicStatus.ENROLLED)) {
                    isAcademicStatusValid = this.currentCompletedSemester != null && this.currentCompletedSemester > 0;
                } else if (this.academicStatus.equals(AcademicStatus.GRADUATED)) {
                    isAcademicStatusValid = this.graduationYear != null && this.graduationYear > 0 && this.graduationType != null;
                }
            }
            boolean isPhoneNumberValid = this.phoneNumber != null;

            return isUserNameValid && isStudentIdValid && isAdmissionYearValid && isMajorValid && isAcademicStatusValid && isPhoneNumberValid;
        }

        // userId가 있으면 다른 값들은 null이어야 함
        return this.userName == null &&
                this.studentId == null &&
                this.admissionYear == null &&
                this.major == null &&
                this.academicStatus == null &&
                this.currentCompletedSemester == null &&
                this.graduationYear == null &&
                this.graduationType == null &&
                this.phoneNumber == null;
    }

    @AssertTrue(message = "isRefunded가 true일 때 refundedAt 값은 null이 아니고 자연수여야 합니다.")
    public boolean isValidRefundedAt() {
        return !this.isRefunded || (this.refundedAt != null && this.refundedAt > 0);
    }
}
