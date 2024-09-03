package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.AcademicRecordRequestStatus;
import net.causw.domain.model.enums.AcademicStatus;

@Getter
@Builder
@Entity
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_academic_record_log")
public class UserAcademicRecordLog extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "controlled_user_id", nullable = false)
    private User controlledUserId;

    @ManyToOne
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUserId;

    @Column(name = "prior_academic_record_application_id", nullable = false)
    private AcademicStatus targetAcademicRecordStatus;

    @ManyToOne
    @JoinColumn(name = "target_user_academic_record_application_id", nullable = true)
    private UserAcademicRecordApplication targetUserAcademicRecordApplicationId;

    @Column(name = "target_academic_record_request_status", nullable = true)
    private AcademicRecordRequestStatus targetAcademicRecordRequestStatus;

    @Column(name = "note", nullable = true)
    private String note;

}
