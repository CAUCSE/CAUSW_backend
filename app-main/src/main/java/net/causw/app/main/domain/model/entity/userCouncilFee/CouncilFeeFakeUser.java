package net.causw.app.main.domain.model.entity.userCouncilFee;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.model.enums.user.GraduationType;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_council_fee_fake_user")
public class CouncilFeeFakeUser extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Column(name = "major", nullable = false)
    private String major;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_status", nullable = false)
    private AcademicStatus academicStatus;

    @Column(name = "current_completed_semester", nullable = true)
    private Integer currentCompletedSemester;

    @Column(name = "graduation_year", nullable = true)
    private Integer graduationYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "graduation_type", nullable = true)
    private GraduationType graduationType;

    public void setCurrentCompletedSemester(Integer currentCompletedSemester) {
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
