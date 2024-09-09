package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.AcademicRecordRequestStatus;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.util.MessageUtil;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_academic_record_admission")
public class UserAcademicRecordApplication extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_academic_record_application_id", nullable = true)
    private List<UuidFile> uuidFileList;

    @Column(name = "reject_message", nullable = true)
    private String rejectMessage;

    public void updateApplicationRequestStatus(AcademicRecordRequestStatus academicRecordRequestStatus, String rejectMessage) {
        if (academicRecordRequestStatus == AcademicRecordRequestStatus.ACCEPT) {
            this.academicRecordRequestStatus = academicRecordRequestStatus;
        } else if (academicRecordRequestStatus == AcademicRecordRequestStatus.REJECT || academicRecordRequestStatus == AcademicRecordRequestStatus.CLOSE) {
            this.academicRecordRequestStatus = academicRecordRequestStatus;
            this.rejectMessage = rejectMessage;
        } else {
            throw new BadRequestException(ErrorCode.INVALID_ACADEMIC_RECORD_REQUEST_STATUS, MessageUtil.INVALID_ACADEMIC_RECORD_REQUEST_STATUS);
        }
    }

    public static UserAcademicRecordApplication createApplication(
            User user,
            AcademicStatus academicStatus,
            Integer targetCompletedSemester,
            String note,
            List<UuidFile> uuidFileList
    ) {
        UserAcademicRecordApplication userAcademicRecordApplication = UserAcademicRecordApplication.builder()
                .user(user)
                .academicRecordRequestStatus(AcademicRecordRequestStatus.AWAIT)
                .targetAcademicStatus(academicStatus)
                .targetCompletedSemester(targetCompletedSemester)
                .note(note)
                .uuidFileList(uuidFileList)
                .build();

        if (academicStatus.equals(AcademicStatus.ENROLLED)) {
            if (targetCompletedSemester == null || targetCompletedSemester < 1) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_TARGET_COMPLETED_SEMESTER);
            }
            if (uuidFileList == null || uuidFileList.isEmpty()) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_UPLOAD_REQUIRED);
            }
        } else {
            if (targetCompletedSemester != null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_TARGET_COMPLETED_SEMESTER);
            }
            if (uuidFileList != null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_UPLOAD_NOT_ALLOWED);
            }
            userAcademicRecordApplication.getUser().setAcademicStatus(academicStatus);
            userAcademicRecordApplication.updateApplicationRequestStatus(AcademicRecordRequestStatus.ACCEPT, null);
        }

        return userAcademicRecordApplication;
    }

}
