package net.causw.app.main.domain.model.entity.userAcademicRecord;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserAcademicRecordApplicationAttachImage;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "tb_user_academic_record_application",
        indexes = {
                @Index(name = "user_id_index", columnList = "user_id")
        })
public class UserAcademicRecordApplication extends BaseEntity {

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "academic_record_request_status", nullable = false)
    private AcademicRecordRequestStatus academicRecordRequestStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_academic_status", nullable = false)
    private AcademicStatus targetAcademicStatus;

    @Column(name = "target_completed_semester", nullable = true)
    private Integer targetCompletedSemester;

    @Column(name = "note", nullable = true)
    private String note;

    @Setter(value = AccessLevel.PRIVATE)
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "userAcademicRecordApplication")
    @Builder.Default
    private List<UserAcademicRecordApplicationAttachImage> userAcademicRecordAttachImageList = new ArrayList<>();

    @Setter
    @Column(name = "reject_message", nullable = true)
    private String rejectMessage;

    public static UserAcademicRecordApplication create(
            User user,
            AcademicRecordRequestStatus academicRecordRequestStatus,
            AcademicStatus academicStatus,
            Integer targetCompletedSemester,
            String note
    ) {
        return UserAcademicRecordApplication.builder()
                .user(user)
                .academicRecordRequestStatus(academicRecordRequestStatus)
                .targetAcademicStatus(academicStatus)
                .targetCompletedSemester(targetCompletedSemester)
                .note(note)
                .build();
    }

    public static UserAcademicRecordApplication createWithImage(
            User user,
            AcademicRecordRequestStatus academicRecordRequestStatus,
            AcademicStatus academicStatus,
            Integer targetCompletedSemester,
            String note,
            List<UuidFile> userAcademicRecordAttachImageUuidFileList
    ) {
        UserAcademicRecordApplication userAcademicRecordApplication = UserAcademicRecordApplication.builder()
                .user(user)
                .academicRecordRequestStatus(academicRecordRequestStatus)
                .targetAcademicStatus(academicStatus)
                .targetCompletedSemester(targetCompletedSemester)
                .note(note)
                .build();

        List<UserAcademicRecordApplicationAttachImage> userAcademicRecordApplicationAttachImageList = userAcademicRecordAttachImageUuidFileList.stream()
                .map(uuidFile -> UserAcademicRecordApplicationAttachImage.of(userAcademicRecordApplication, uuidFile))
                .toList();

        userAcademicRecordApplication.setUserAcademicRecordAttachImageList(userAcademicRecordApplicationAttachImageList);

        return userAcademicRecordApplication;
    }

}
