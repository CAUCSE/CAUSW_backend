package net.causw.adapter.persistence.userCouncilFee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.semester.Semester;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.GraduationType;
import net.causw.domain.model.enums.LogType;
import net.causw.domain.model.enums.SemesterType;

import java.time.LocalDate;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_user_council_fee_log")
public class UserCouncilFeeLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controlled_user_id", nullable = false)
    private User controlledUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "update_type", nullable = false)
    private LogType logType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_council_fee_id", nullable = false)
    private UserCouncilFee targetUserCouncilFee;

    @Column(name = "target_is_joined_service", nullable = false)
    private Boolean targetIsJoinedService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = true)
    private User targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_council_fee_fake_user_id", nullable = true)
    private CouncilFeeFakeUser targetCouncilFeeFakeUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_of_semester_id", nullable = false)
    private Semester timeOfSemester;

    @Column(name = "time_of_semester_year", nullable = false)
    private Integer timeOfSemesterYear;

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

    @Column(name = "academic_status", nullable = false)
    private AcademicStatus academicStatus;

    @Column(name = "current_completed_semester", nullable = false)
    private Integer currentCompletedSemester;

    @Column(name = "graduation_year", nullable = true)
    private Integer graduationYear;

    @Column(name = "graduation_type", nullable = true)
    private GraduationType graduationType;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "joined_at", nullable = true)
    private LocalDate joinedAt;

    @Column(name = "paid_at", nullable = false)
    private Integer restOfSemester;

    @Column(name = "num_of_paid_semester", nullable = false)
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
            LogType logType,
            UserCouncilFee userCouncilFee,
            Semester semester,
            User targetUser,
            Integer restOfSemester,
            Boolean isAppliedThisSemester
    ) {
        return UserCouncilFeeLog.builder()
                .controlledUser(controlledUser)
                .logType(logType)
                .targetUserCouncilFee(userCouncilFee)
                .targetIsJoinedService(userCouncilFee.getIsJoinedService())
                .targetUser(targetUser)
                .timeOfSemester(semester)
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
            LogType logType,
            UserCouncilFee userCouncilFee,
            Semester semester,
            CouncilFeeFakeUser targetCouncilFeeFakeUser,
            Integer restOfSemester,
            Boolean isAppliedThisSemester
    ) {
        return UserCouncilFeeLog.builder()
                .controlledUser(controlledUser)
                .logType(logType)
                .targetUserCouncilFee(userCouncilFee)
                .targetIsJoinedService(userCouncilFee.getIsJoinedService())
                .targetCouncilFeeFakeUser(targetCouncilFeeFakeUser)
                .timeOfSemester(semester)
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
