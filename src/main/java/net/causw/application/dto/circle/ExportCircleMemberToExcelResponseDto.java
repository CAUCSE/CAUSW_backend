package net.causw.application.dto.circle;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.enums.user.GraduationType;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ExportCircleMemberToExcelResponseDto {

    @Schema(description = "아이디(이메일)", example = "example@cau.ac.kr")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "닉네임", example = "홍길동123")
    private String nickname;

    @Schema(description = "입학년도", example = "2020")
    private Integer admissionYear;

    @Schema(description = "학번", example = "20209999")
    private String studentId;

    @Schema(description = "학부/학과", example = "소프트웨어학부")
    private String major;

    @Schema(description = "연락처", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "학적 상태", example = "ENROLLED")
    private AcademicStatus academicStatus;

    @Schema(description = "현재 등록 완료된 학기", example = "6(3학년 2학기)")
    private Integer currentSemester;

    @Schema(description = "졸업 년도", example = "2024")
    private Integer graduationYear;

    @Schema(description = "졸업 시기", example = "2")
    private GraduationType graduationType;

    @Schema(description = "동문네트워크 가입일", example = "2024-03-24")
    private LocalDateTime createdAt;

    @Schema(description = "본 학기 학생회비 납부 여부", example = "true")
    private Boolean isAppliedThisSemester;

    @Schema(description = "학생회비 납부 시점", example = "1차 학기")
    private Integer paidAt;

    @Schema(description = "학생회비 납부 차수", example = "5차 학기 분")
    private Integer paidSemester;

    @Schema(description = "적용 학생회비 학기", example = "5차 학기 적용")
    private Integer appliedSemester;

    @Schema(description = "잔여 학생회비 적용 학기", example = "3차 학기 잔여")
    private Integer restOfSemester;

    @Schema(description = "학생회비 환불 여부", example = "false")
    private Boolean isRefunded;

}
