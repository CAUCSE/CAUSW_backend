package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.user.UserAdmission;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_user_admission_attach_image_uuid_file",
indexes = {
    @Index(name = "idx_user_admission_attach_image__admission_id", columnList = "user_admission_id"),
    @Index(name = "idx_user_admission_attach_image_uuid_file_id", columnList = "uuid_file_id")
})
public class UserAdmissionAttachImage extends JoinEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_admission_id", nullable = false)
    private UserAdmission userAdmission;

    private UserAdmissionAttachImage(UserAdmission userAdmission, UuidFile uuidFile) {
        super(uuidFile);
        this.userAdmission = userAdmission;
    }

    public static UserAdmissionAttachImage of(UserAdmission userAdmission, UuidFile uuidFile) {
        return new UserAdmissionAttachImage(userAdmission, uuidFile);
    }

}
