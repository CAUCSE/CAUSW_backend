package net.causw.adapter.persistence.uuidFile;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.userAcademicRecord.UserAcademicRecordApplication;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_user_academic_record_application_attach_image_uuid_file")
public class UserAcademicRecordApplicationAttachImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_academic_record_application_id", nullable = false)
    private UserAcademicRecordApplication userAcademicRecordApplication;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false)
    private UuidFile uuidFile;

    public static UserAcademicRecordApplicationAttachImage of(UserAcademicRecordApplication userAcademicRecordApplication, UuidFile uuidFile) {
        return UserAcademicRecordApplicationAttachImage.builder()
            .userAcademicRecordApplication(userAcademicRecordApplication)
            .uuidFile(uuidFile)
            .build();
    }
}
