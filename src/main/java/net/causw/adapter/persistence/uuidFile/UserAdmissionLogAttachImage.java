package net.causw.adapter.persistence.uuidFile;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.UserAdmissionLog;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_user_admission_log_attach_image_uuid_file")
public class UserAdmissionLogAttachImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_admission_log_id", nullable = false)
    private UserAdmissionLog userAdmissionLog;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false)
    private UuidFile uuidFile;

    public static UserAdmissionLogAttachImage of(UserAdmissionLog userAdmissionLog, UuidFile uuidFile) {
        return UserAdmissionLogAttachImage.builder()
            .userAdmissionLog(userAdmissionLog)
            .uuidFile(uuidFile)
            .build();
    }

}
