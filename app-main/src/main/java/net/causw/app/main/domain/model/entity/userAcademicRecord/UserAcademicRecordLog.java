package net.causw.app.main.domain.model.entity.userAcademicRecord;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserAcademicRecordLogAttachImage;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.model.enums.user.GraduationType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_academic_record_log")
public class UserAcademicRecordLog extends BaseEntity {

    @Column(name = "controlled_user_email", nullable = false)
    private String controlledUserEmail;

    @Column(name = "controlled_user_name", nullable = false)
    private String controlledUserName;

    @Column(name = "controlled_user_student_id", nullable = false)
    private String controlledUserStudentId;

    @Column(name = "target_user_email", nullable = false)
    private String targetUserEmail;

    @Column(name = "target_user_name", nullable = false)
    private String targetUserName;

    @Column(name = "target_user_student_id", nullable = false)
    private String targetUserStudentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "prior_academic_record_application_id", nullable = false)
    private AcademicStatus targetAcademicRecordStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_academic_record_request_status", nullable = true)
    private AcademicRecordRequestStatus targetAcademicRecordRequestStatus;

    @Setter(value = AccessLevel.PRIVATE)
    @OneToMany(cascade = { CascadeType.PERSIST }, mappedBy = "userAcademicRecordLog")
    @Builder.Default
    private List<UserAcademicRecordLogAttachImage> userAcademicRecordLogAttachImageList = new ArrayList<>();

    @Column(name = "graduation_year", nullable = true)
    private Integer graduationYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "graduation_type", nullable = true)
    private GraduationType graduationType;

    @Column(name = "note", nullable = true)
    private String note;

    @Column(name = "reject_message", nullable = true)
    private String rejectMessage;

    public static UserAcademicRecordLog createWithApplication(
            User controlledUser,
            UserAcademicRecordApplication targetUserAcademicRecordApplication
    ) {
        User targetUser = targetUserAcademicRecordApplication.getUser();

        UserAcademicRecordLog userAcademicRecordLog = UserAcademicRecordLog.builder()
                .controlledUserEmail(controlledUser.getEmail())
                .controlledUserName(controlledUser.getName())
                .controlledUserStudentId(controlledUser.getStudentId())
                .targetUserEmail(targetUser.getEmail())
                .targetUserName(targetUser.getName())
                .targetUserStudentId(targetUser.getStudentId())
                .targetAcademicRecordStatus(targetUserAcademicRecordApplication.getTargetAcademicStatus())
                .targetAcademicRecordRequestStatus(targetUserAcademicRecordApplication.getAcademicRecordRequestStatus())
                .note(targetUserAcademicRecordApplication.getNote())
                .rejectMessage(targetUserAcademicRecordApplication.getRejectMessage())
                .build();

        List<UserAcademicRecordLogAttachImage> userAcademicRecordLogAttachImageList =
                targetUserAcademicRecordApplication.getUserAcademicRecordAttachImageList()
                        .stream()
                        .map(userAcademicRecordApplicationAttachImage ->
                                UserAcademicRecordLogAttachImage.of(
                                        userAcademicRecordLog, userAcademicRecordApplicationAttachImage.getUuidFile()))
                        .toList();

        userAcademicRecordLog.setUserAcademicRecordLogAttachImageList(userAcademicRecordLogAttachImageList);

        return userAcademicRecordLog;
    }

    public static UserAcademicRecordLog createWithApplication(
            User controlledUser,
            UserAcademicRecordApplication targetUserAcademicRecordApplication,
            String note
    ) {
        User targetUser = targetUserAcademicRecordApplication.getUser();

        UserAcademicRecordLog userAcademicRecordLog = UserAcademicRecordLog.builder()
                .controlledUserEmail(controlledUser.getEmail())
                .controlledUserName(controlledUser.getName())
                .controlledUserStudentId(controlledUser.getStudentId())
                .targetUserEmail(targetUser.getEmail())
                .targetUserName(targetUser.getName())
                .targetUserStudentId(targetUser.getStudentId())
                .targetAcademicRecordStatus(targetUserAcademicRecordApplication.getTargetAcademicStatus())
                .targetAcademicRecordRequestStatus(targetUserAcademicRecordApplication.getAcademicRecordRequestStatus())
                .note(note)
                .rejectMessage(targetUserAcademicRecordApplication.getRejectMessage())
                .build();

        List<UserAcademicRecordLogAttachImage> userAcademicRecordLogAttachImageList =
                targetUserAcademicRecordApplication.getUserAcademicRecordAttachImageList()
                        .stream()
                        .map(userAcademicRecordApplicationAttachImage ->
                                UserAcademicRecordLogAttachImage.of(
                                        userAcademicRecordLog, userAcademicRecordApplicationAttachImage.getUuidFile()))
                        .toList();

        userAcademicRecordLog.setUserAcademicRecordLogAttachImageList(userAcademicRecordLogAttachImageList);

        return userAcademicRecordLog;
    }

    public static UserAcademicRecordLog create(
            User controlledUser,
            User targetUser,
            AcademicStatus targetAcademicRecordStatus,
            String note
    ) {
        return UserAcademicRecordLog.builder()
                .controlledUserEmail(controlledUser.getEmail())
                .controlledUserName(controlledUser.getName())
                .controlledUserStudentId(controlledUser.getStudentId())
                .targetUserEmail(targetUser.getEmail())
                .targetUserName(targetUser.getName())
                .targetUserStudentId(targetUser.getStudentId())
                .targetAcademicRecordStatus(targetAcademicRecordStatus)
                .note(note)
                .build();
    }

    public static UserAcademicRecordLog createWithGraduation(
            User controlledUser,
            User targetUser,
            AcademicStatus targetAcademicRecordStatus,
            Integer graduationYear,
            GraduationType graduationType,
            String note
    ) {
        return UserAcademicRecordLog.builder()
                .controlledUserEmail(controlledUser.getEmail())
                .controlledUserName(controlledUser.getName())
                .controlledUserStudentId(controlledUser.getStudentId())
                .targetUserEmail(targetUser.getEmail())
                .targetUserName(targetUser.getName())
                .targetUserStudentId(targetUser.getStudentId())
                .targetAcademicRecordStatus(targetAcademicRecordStatus)
                .graduationYear(graduationYear)
                .graduationType(graduationType)
                .note(note)
                .build();
    }
}
