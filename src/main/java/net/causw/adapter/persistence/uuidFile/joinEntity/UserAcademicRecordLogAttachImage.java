package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.userAcademicRecord.UserAcademicRecordLog;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_user_academic_record_log_attach_image",
indexes = {
    @Index(name = "idx_user_academic_record_log_attach_image_log_id", columnList = "user_academic_record_log_id"),
    @Index(name = "idx_user_academic_record_log_attach_image_uuid_file_id", columnList = "uuid_file_id")
})
public class UserAcademicRecordLogAttachImage extends JoinEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_academic_record_log_id", nullable = false)
    private UserAcademicRecordLog userAcademicRecordLog;

    private UserAcademicRecordLogAttachImage(UserAcademicRecordLog userAcademicRecordLog, UuidFile uuidFile) {
        super(uuidFile);
        this.userAcademicRecordLog = userAcademicRecordLog;
    }

    public static UserAcademicRecordLogAttachImage of(UserAcademicRecordLog userAcademicRecordLog, UuidFile uuidFile) {
        return new UserAcademicRecordLogAttachImage(userAcademicRecordLog, uuidFile);
    }

}
