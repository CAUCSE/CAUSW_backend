package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.AcademicStatus;

import java.util.List;

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_academic_record_admission")
public class UserAcademicRecordAdmission extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_academic_status", nullable = false)
    private AcademicStatus targetAcademicStatus;

    @Column(name = "target_completed_semester", nullable = true)
    private Integer targetCompletedSemester;

    @Column(name = "note", nullable = true)
    private String note;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "attach_image_list", length = 500, nullable = true)
    private List<String> attachImageList;

}
