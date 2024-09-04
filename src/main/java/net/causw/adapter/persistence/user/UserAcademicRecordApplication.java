package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(name = "academic_record_request_status", nullable = false)
    private AcademicRecordRequestStatus academicRecordRequestStatus;

    @Column(name = "target_academic_status", nullable = false)
    private AcademicStatus targetAcademicStatus;

    @Column(name = "target_completed_semester", nullable = true)
    private Integer targetCompletedSemester;

    @Column(name = "note", nullable = true)
    private String note;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "attach_image_url_list", length = 500, nullable = true)
    private List<String> attachImageUrlList;

    @Column(name = "reject_message", nullable = true)
    private String rejectMessage;

    public void updateApplication(AcademicRecordRequestStatus academicRecordRequestStatus, String rejectMessage) {
        if (academicRecordRequestStatus == AcademicRecordRequestStatus.ACCEPT) {
            this.academicRecordRequestStatus = academicRecordRequestStatus;
        } else if (academicRecordRequestStatus == AcademicRecordRequestStatus.REJECT) {
            this.academicRecordRequestStatus = academicRecordRequestStatus;
            this.rejectMessage = rejectMessage;
        } else {
            throw new BadRequestException(ErrorCode.INVALID_ACADEMIC_RECORD_REQUEST_STATUS, MessageUtil.INVALID_ACADEMIC_RECORD_REQUEST_STATUS);
        }
    }

}
