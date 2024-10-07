package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_user_academic_record_application_attach_image_uuid_file",
indexes = {
    @Index(name = "idx_user_academic_record_application_attach_image_application_id", columnList = "user_academic_record_application_id"),
    @Index(name = "idx_user_academic_record_application_attach_image_uuid_file_id", columnList = "uuid_file_id")
})
public class UserAcademicRecordApplicationAttachImage extends JoinEntity {

    @Getter
    @Setter(AccessLevel.PUBLIC)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
    public UuidFile uuidFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_academic_record_application_id", nullable = false)
    private UserAcademicRecordApplication userAcademicRecordApplication;

    public static UserAcademicRecordApplicationAttachImage of(UserAcademicRecordApplication userAcademicRecordApplication, UuidFile uuidFile) {
        return UserAcademicRecordApplicationAttachImage.builder()
                .uuidFile(uuidFile)
                .userAcademicRecordApplication(userAcademicRecordApplication)
                .build();
    }

}
