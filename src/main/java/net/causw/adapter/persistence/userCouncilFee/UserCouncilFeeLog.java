package net.causw.adapter.persistence.userCouncilFee;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.semester.Semester;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.enums.user.GraduationType;
import net.causw.domain.model.enums.userCouncilFee.CouncilFeeLogType;
import net.causw.domain.model.enums.semester.SemesterType;

import java.time.LocalDate;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_user_council_fee_log")
public class UserCouncilFeeLog extends BaseEntity {

    @Column(name = "controlled_user_email", nullable = false)
    private String controlledUserEmail;

    @Column(name = "controlled_user_name", nullable = false)
    private String controlledUserName;

    @Column(name = "controlled_user_student_id", nullable = false)
    private String controlledUserStudentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "council_fee_log_type", nullable = false)
    private CouncilFeeLogType councilFeeLogType;

    @Column(name = "target_is_joined_service", nullable = false)
    private Boolean targetIsJoinedService;

    @Column(name = "time_of_semester_year", nullable = false)
    private Integer timeOfSemesterYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_of_semester_type", nullable = false)
    private SemesterType timeOfSemesterType;

    @Column(name = "email", nullable = true)
    private String email;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Column(name = "major", nullable = false)
    private String major;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_status", nullable = false)
    private AcademicStatus academicStatus;

    @Column(name = "current_completed_semester", nullable = false)
    private Integer currentCompletedSemester;

    @Column(name = "graduation_year", nullable = true)
    private Integer graduationYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "graduation_type", nullable = true)
    private GraduationType graduationType;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "joined_at", nullable = true)
    private LocalDate joinedAt;

    @Column(name = "rest_of_semester", nullable = false)
    private Integer restOfSemester;

    @Column(name = "is_applied_this_semester", nullable = false)
    private Boolean isAppliedThisSemester;

    @Column(name = "target_paid_at", nullable = false)
    private Integer targetPaidAt;

    @Column(name = "target_num_of_paid_semester", nullable = false)
    private Integer targetNumOfPaidSemester;

    @Column(name = "target_is_refunded", nullable = false)
    private Boolean targetIsRefunded;

    @Column(name = "target_refunded_at", nullable = true)
    private Integer targetRefundedAt;

    public static UserCouncilFeeLog fromUser(
            User controlledUser,
            CouncilFeeLogType councilFeeLogType,
            UserCouncilFee userCouncilFee,
            Semester semester,
            User targetUser,
            Integer restOfSemester,
            Boolean isAppliedThisSemester
    ) {
        return UserCouncilFeeLog.builder()
                .controlledUserEmail(controlledUser.getEmail())
                .controlledUserName(controlledUser.getName())
                .controlledUserStudentId(controlledUser.getStudentId())
                .councilFeeLogType(councilFeeLogType)
                .targetIsJoinedService(userCouncilFee.getIsJoinedService())
                .timeOfSemesterYear(semester.getSemesterYear())
                .timeOfSemesterType(semester.getSemesterType())
                .email(targetUser.getEmail())
                .userName(targetUser.getName())
                .studentId(targetUser.getStudentId())
                .admissionYear(targetUser.getAdmissionYear())
                .major(targetUser.getMajor())
                .academicStatus(targetUser.getAcademicStatus())
                .currentCompletedSemester(targetUser.getCurrentCompletedSemester())
                .graduationYear(targetUser.getGraduationYear())
                .graduationType(targetUser.getGraduationType())
                .phoneNumber(targetUser.getPhoneNumber())
                .joinedAt(targetUser.getCreatedAt().toLocalDate())
                .restOfSemester(restOfSemester)
                .isAppliedThisSemester(isAppliedThisSemester)
                .targetPaidAt(userCouncilFee.getPaidAt())
                .targetNumOfPaidSemester(userCouncilFee.getNumOfPaidSemester())
                .targetIsRefunded(userCouncilFee.getIsRefunded())
                .targetRefundedAt(userCouncilFee.getRefundedAt())
                .build();
    }

    public static UserCouncilFeeLog fromCouncilFeeFakeUser(
            User controlledUser,
            CouncilFeeLogType councilFeeLogType,
            UserCouncilFee userCouncilFee,
            Semester semester,
            CouncilFeeFakeUser targetCouncilFeeFakeUser,
            Integer restOfSemester,
            Boolean isAppliedThisSemester
    ) {
        return UserCouncilFeeLog.builder()
                .controlledUserEmail(controlledUser.getEmail())
                .controlledUserName(controlledUser.getName())
                .controlledUserStudentId(controlledUser.getStudentId())
                .councilFeeLogType(councilFeeLogType)
                .targetIsJoinedService(userCouncilFee.getIsJoinedService())
                .timeOfSemesterYear(semester.getSemesterYear())
                .timeOfSemesterType(semester.getSemesterType())
                .userName(targetCouncilFeeFakeUser.getName())
                .studentId(targetCouncilFeeFakeUser.getStudentId())
                .admissionYear(targetCouncilFeeFakeUser.getAdmissionYear())
                .major(targetCouncilFeeFakeUser.getMajor())
                .academicStatus(targetCouncilFeeFakeUser.getAcademicStatus())
                .currentCompletedSemester(targetCouncilFeeFakeUser.getCurrentCompletedSemester())
                .graduationYear(targetCouncilFeeFakeUser.getGraduationYear())
                .graduationType(targetCouncilFeeFakeUser.getGraduationType())
                .phoneNumber(targetCouncilFeeFakeUser.getPhoneNumber())
                .restOfSemester(restOfSemester)
                .isAppliedThisSemester(isAppliedThisSemester)
                .targetPaidAt(userCouncilFee.getPaidAt())
                .targetNumOfPaidSemester(userCouncilFee.getNumOfPaidSemester())
                .targetIsRefunded(userCouncilFee.getIsRefunded())
                .targetRefundedAt(userCouncilFee.getRefundedAt())
                .build();
    }
}
