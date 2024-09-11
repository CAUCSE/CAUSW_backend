package net.causw.adapter.persistence.userCouncilFee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.GraduationType;
import net.causw.domain.model.util.MessageUtil;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CouncilFeeFakeUser extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "email", nullable = false)
    private String phoneNumber;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Column(name = "nickname", nullable = false)
    private String major;

    @Enumerated(EnumType.STRING)
    @Column(name = "major", nullable = false)
    private AcademicStatus academicStatus;

    @Column(name = "current_completed_semester", nullable = true)
    private Integer currentCompletedSemester;

    @Column(name = "academic_status_note", nullable = true)
    private Integer graduationYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "graduation_type", nullable = true)
    private GraduationType graduationType;

    public void setCurrentCompletedSemester(Integer currentCompletedSemester) {
        if (this.academicStatus.equals(AcademicStatus.ENROLLED) && currentCompletedSemester == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_COUNCIL_FEE_FAKE_USER_INFO);
        }
        this.currentCompletedSemester = currentCompletedSemester;
    }

    public void update(
            String name,
            String studentId,
            String phoneNumber,
            Integer admissionYear,
            String major,
            AcademicStatus academicStatus,
            Integer currentCompletedSemester,
            Integer graduationYear,
            GraduationType graduationType
    ) {
        if (
                (academicStatus.equals(AcademicStatus.ENROLLED) && currentCompletedSemester == null) ||
                        (academicStatus.equals(AcademicStatus.GRADUATED) && ( graduationYear == null || graduationType == null ))
        ) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_COUNCIL_FEE_FAKE_USER_INFO);
        }

        this.name = name;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.admissionYear = admissionYear;
        this.major = major;
        this.academicStatus = academicStatus;
        this.currentCompletedSemester = currentCompletedSemester;
        this.graduationYear = graduationYear;
        this.graduationType = graduationType;
    }


    public static CouncilFeeFakeUser of(
            String name,
            String studentId,
            String phoneNumber,
            Integer admissionYear,
            String major,
            AcademicStatus academicStatus,
            Integer currentCompletedSemester,
            Integer graduationYear,
            GraduationType graduationType
    ) {
        if (
                (academicStatus.equals(AcademicStatus.ENROLLED) && currentCompletedSemester == null) ||
                        (academicStatus.equals(AcademicStatus.GRADUATED) && ( graduationYear == null || graduationType == null ))
        ) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_COUNCIL_FEE_FAKE_USER_INFO);
        }
        return CouncilFeeFakeUser.builder()
                .name(name)
                .studentId(studentId)
                .phoneNumber(phoneNumber)
                .admissionYear(admissionYear)
                .major(major)
                .academicStatus(academicStatus)
                .currentCompletedSemester(currentCompletedSemester)
                .graduationYear(graduationYear)
                .graduationType(graduationType)
                .build();
    }

}
