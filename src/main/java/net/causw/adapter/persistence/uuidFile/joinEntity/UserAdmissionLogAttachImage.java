package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.UserAdmissionLog;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_user_admission_log_attach_image_uuid_file",
indexes = {
    @Index(name = "idx_user_admission_log_attach_image_log_id", columnList = "user_admission_log_id"),
    @Index(name = "idx_user_admission_log_attach_image_uuid_file_id", columnList = "uuid_file_id")
})
public class UserAdmissionLogAttachImage extends JoinEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_admission_log_id", nullable = false)
    private UserAdmissionLog userAdmissionLog;

    private UserAdmissionLogAttachImage(UserAdmissionLog userAdmissionLog, UuidFile uuidFile) {
        super(uuidFile);
        this.userAdmissionLog = userAdmissionLog;
    }

    public static UserAdmissionLogAttachImage of(UserAdmissionLog userAdmissionLog, UuidFile uuidFile) {
        return new UserAdmissionLogAttachImage(userAdmissionLog, uuidFile);
    }

}
