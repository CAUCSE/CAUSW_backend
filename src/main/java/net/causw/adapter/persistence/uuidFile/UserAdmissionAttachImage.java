package net.causw.adapter.persistence.uuidFile;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.UserAdmission;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_user_admission_attach_image_uuid_file")
public class UserAdmissionAttachImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_admission_id", nullable = false)
    private UserAdmission userAdmission;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false)
    private UuidFile uuidFile;

    public static UserAdmissionAttachImage of(UserAdmission userAdmission, UuidFile uuidFile) {
        return UserAdmissionAttachImage.builder()
            .userAdmission(userAdmission)
            .uuidFile(uuidFile)
            .build();
    }

}
