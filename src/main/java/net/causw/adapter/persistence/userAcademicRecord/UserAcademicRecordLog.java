package net.causw.adapter.persistence.userAcademicRecord;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.AcademicRecordRequestStatus;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.GraduationType;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_academic_record_log")
public class UserAcademicRecordLog extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "controlled_user_id", nullable = false)
    private User controlledUser;

    @ManyToOne
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "prior_academic_record_application_id", nullable = false)
    private AcademicStatus targetAcademicRecordStatus;

    @ManyToOne
    @JoinColumn(name = "target_user_academic_record_application_id", nullable = true)
    private UserAcademicRecordApplication targetUserAcademicRecordApplication;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_academic_record_request_status", nullable = true)
    private AcademicRecordRequestStatus targetAcademicRecordRequestStatus;

    @Column(name = "graduation_year", nullable = true)
    private Integer graduationYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "graduation_type", nullable = true)
    private GraduationType graduationType;

    @Column(name = "note", nullable = true)
    private String note;

    public static UserAcademicRecordLog createWithApplicationAndNote(
            User controlledUser,
            User targetUser,
            AcademicStatus targetAcademicRecordStatus,
            UserAcademicRecordApplication targetUserAcademicRecordApplication,
            AcademicRecordRequestStatus targetAcademicRecordRequestStatus,
            String note
    ) {
        return UserAcademicRecordLog.builder()
                .controlledUser(controlledUser)
                .targetUser(targetUser)
                .targetAcademicRecordStatus(targetAcademicRecordStatus)
                .targetUserAcademicRecordApplication(targetUserAcademicRecordApplication)
                .targetAcademicRecordRequestStatus(targetAcademicRecordRequestStatus)
                .note(note)
                .build();
    }

    public static UserAcademicRecordLog createWithApplication(
            User controlledUser,
            User targetUser,
            AcademicStatus targetAcademicRecordStatus,
            UserAcademicRecordApplication targetUserAcademicRecordApplication,
            AcademicRecordRequestStatus targetAcademicRecordRequestStatus
    ) {
        return UserAcademicRecordLog.builder()
                .controlledUser(controlledUser)
                .targetUser(targetUser)
                .targetAcademicRecordStatus(targetAcademicRecordStatus)
                .targetUserAcademicRecordApplication(targetUserAcademicRecordApplication)
                .targetAcademicRecordRequestStatus(targetAcademicRecordRequestStatus)
                .build();
    }

    public static UserAcademicRecordLog createWithNote(
            User controlledUser,
            User targetUser,
            AcademicStatus targetAcademicRecordStatus,
            String note
    ) {
        return UserAcademicRecordLog.builder()
                .controlledUser(controlledUser)
                .targetUser(targetUser)
                .targetAcademicRecordStatus(targetAcademicRecordStatus)
                .note(note)
                .build();
    }

    public static UserAcademicRecordLog create(
            User controlledUser,
            User targetUser,
            AcademicStatus targetAcademicRecordStatus
    ) {
        return UserAcademicRecordLog.builder()
                .controlledUser(controlledUser)
                .targetUser(targetUser)
                .targetAcademicRecordStatus(targetAcademicRecordStatus)
                .build();
    }

    public static UserAcademicRecordLog createWithGraduationWithNote(
            User controlledUser,
            User targetUser,
            AcademicStatus targetAcademicRecordStatus,
            Integer graduationYear,
            GraduationType graduationType,
            String note
    ) {
        return UserAcademicRecordLog.builder()
                .controlledUser(controlledUser)
                .targetUser(targetUser)
                .targetAcademicRecordStatus(targetAcademicRecordStatus)
                .graduationYear(graduationYear)
                .graduationType(graduationType)
                .note(note)
                .build();
    }
}
