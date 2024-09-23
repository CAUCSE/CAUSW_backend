package net.causw.application.dto.userCouncilFee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.enums.user.GraduationType;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class UserCouncilFeeResponseDto {

    @Schema(description = "userCouncilFee 고유 id값", example = "uuid 형식의 String 값입니다.")
    private String userCouncilFeeId;

    @Schema(description = "동문네트워크 서비스 가입 여부", example = "true")
    private Boolean isJoinedService;

    @Schema(description = "user 고유 id값(서비스 가입 시만 존재)", example = "uuid 형식의 String 값입니다.")
    private String userId;

    @Schema(description = "councilFeeFakeUser 고유 id값(서비스 미가입 시만 존재)", example = "uuid 형식의 String 값입니다.")
    private String councilFeeFakeUserId;

    @Schema(description = "이메일(아이디)(서비스 가입 시만 존재)", example = "example@cau.ac.kr")
    private String email;

    @Schema(description = "이름", example = "정상제")
    private String userName;

    @Schema(description = "학번", example = "20191234")
    private String studentId;

    @Schema(description = "입학년도", example = "2019")
    private Integer admissionYear;

    @Schema(description = "전공", example = "컴퓨터공학과")
    private String major;

    @Schema(description = "학적상태", example = "재학")
    private AcademicStatus academicStatus;

    @Schema(description = "등록 완료 학기", example = "5")
    private Integer currentCompletedSemester;

    @Schema(description = "졸업년도", example = "2023")
    private Integer graduationYear;

    @Schema(description = "졸업 유형", example = "졸업예정")
    private GraduationType graduationType;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "동문 네트워크 가입일(서비스 가입 시만 존재)", example = "2021-01-01")
    private LocalDate joinedAt;

    @Schema(description = "납부 시점 학기", example = "1")
    private Integer paidAt;

    @Schema(description = "납부한 학기 수", example = "8")
    private Integer numOfPaidSemester;

    @Schema(description = "환불 여부", example = "false")
    private Boolean isRefunded;

    @Schema(description = "환불 시점(isRefunded가 ture일 때만 존재", example = "2021-01-01")
    private Integer refundedAt;

    @Schema(description = "잔여 학생회비 적용 학기", example = "3")
    private Integer restOfSemester;

    @Schema(description = "본 학기 학생회비 적용 여부", example = "true")
    private Boolean isAppliedThisSemester;

}
